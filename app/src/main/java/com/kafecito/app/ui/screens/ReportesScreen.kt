package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kafecito.app.ui.components.KafeTopBar
import com.kafecito.app.ui.theme.*
import com.kafecito.app.ui.viewmodel.ReportesData
import com.kafecito.app.ui.viewmodel.ReportesUiState
import com.kafecito.app.ui.viewmodel.ReportesViewModel
import com.kafecito.app.util.comoDinero

/**
 * Reportes de ventas del administrador: total vendido, cantidad de pedidos,
 * productos más vendidos, stock bajo y pedidos por estado.
 */
@Composable
fun ReportesScreen(
    onAtras: () -> Unit,
    viewModel: ReportesViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(KafeBlack).padding(20.dp)) {
        KafeTopBar(titulo = "REPORTES", onAtras = onAtras)
        Spacer(modifier = Modifier.height(12.dp))

        when (val e = estado) {
            ReportesUiState.Cargando -> CircularProgressIndicator(color = KafeWhite)

            ReportesUiState.Error -> {
                Text("No se pudieron cargar los reportes", color = KafeGray)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { viewModel.cargar() }) { Text("Reintentar") }
            }

            is ReportesUiState.Listo -> ContenidoReportes(e.datos)
        }
    }
}

@Composable
private fun ContenidoReportes(datos: ReportesData) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Resumen general
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Tarjeta(modifier = Modifier.weight(1f)) {
                Text("Pedidos realizados", color = KafeGray, style = MaterialTheme.typography.bodySmall)
                Text(
                    "${datos.cantidadPedidos}",
                    color = KafeBlack,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Tarjeta(modifier = Modifier.weight(1f)) {
                Text("Total vendido", color = KafeGray, style = MaterialTheme.typography.bodySmall)
                Text(
                    datos.totalVendido.comoDinero(),
                    color = KafeBlack,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        // Más vendidos
        Seccion("Productos más vendidos") {
            if (datos.masVendidos.isEmpty()) {
                Text("Aún no hay ventas", color = KafeGray)
            } else {
                datos.masVendidos.forEachIndexed { indice, (nombre, cantidad) ->
                    Fila("${indice + 1}. $nombre", "$cantidad uds.")
                }
            }
        }

        // Stock bajo
        Seccion("Productos con poco stock") {
            if (datos.stockBajo.isEmpty()) {
                Text("Todo el inventario está en buen nivel", color = KafeGray)
            } else {
                datos.stockBajo.forEach { producto ->
                    Fila(producto.nombre, "Stock: ${producto.stock}")
                }
            }
        }

        // Pedidos por estado
        Seccion("Pedidos por estado") {
            datos.pedidosPorEstado.forEach { (estado, cantidad) ->
                Fila(estado.lowercase().replaceFirstChar { it.uppercase() }, "$cantidad")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/** Contenedor con el estilo de tarjeta de la app. */
@Composable
private fun Tarjeta(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(KafeSurface)
            .padding(14.dp),
        content = contenido
    )
}

/** Sección con título dentro de una tarjeta a todo el ancho. */
@Composable
private fun Seccion(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Tarjeta(modifier = Modifier.fillMaxWidth()) {
        Text(titulo, color = KafeBlack, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        contenido()
    }
}

/** Fila con etiqueta a la izquierda y valor a la derecha. */
@Composable
private fun Fila(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(etiqueta, color = KafeGray, style = MaterialTheme.typography.bodyMedium)
        Text(valor, color = KafeGray, style = MaterialTheme.typography.bodyMedium)
    }
}