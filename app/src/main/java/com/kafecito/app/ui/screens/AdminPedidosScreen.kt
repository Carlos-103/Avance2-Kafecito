package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kafecito.app.data.model.EstadoPedido
import com.kafecito.app.ui.components.KafeTopBar
import com.kafecito.app.ui.theme.*
import com.kafecito.app.util.comoDinero
import com.kafecito.app.util.comoFecha
import com.kafecito.app.viewmodel.AdminPedidosViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign

/**
 * Muestra los pedidos de todos los clientes para su gestión administrativa.
 * Permite filtrar los pedidos por estado y actualizar su estado.
 */
@Composable
fun AdminPedidosScreen(onAtras: () -> Unit) {
    val vm: AdminPedidosViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }

LaunchedEffect(vm.error) {
    // Si la lista está vacía, el error ya se muestra en pantalla con "Reintentar".
    if (vm.pedidos.isNotEmpty()) {
        vm.error?.let { snackbarHostState.showSnackbar(it) }
    }
}
    Scaffold(
        containerColor = KafeBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            KafeTopBar(titulo = "PEDIDOS DEL LOCAL", onAtras = onAtras)
            Spacer(modifier = Modifier.height(12.dp))

            if (vm.pedidos.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (listOf(null to "Todos") + EstadoPedido.todos.map { estado ->
                        estado to estado.etiquetaEstado()
                    }).forEach { (estado, etiqueta) ->
                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = vm.filtroEstado == estado,
                            onClick = { vm.filtrarPorEstado(estado) },
                            label = {
                                Text(
                                    text = etiqueta,
                                    maxLines = 1,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = KafeCard,
                                labelColor = KafeGray,
                                selectedContainerColor = KafeWhite,
                                selectedLabelColor = KafeBlack
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            when {
                vm.cargando && vm.pedidos.isEmpty() ->
                    CircularProgressIndicator(color = KafeWhite)

                vm.error != null && vm.pedidos.isEmpty() -> Column {
                    Text(vm.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { vm.cargar() }) {
                        Text("Reintentar", color = KafeWhite)
                    }
                }

                vm.pedidos.isEmpty() -> Text("Todavía no hay pedidos", color = KafeGray)

                vm.pedidosFiltrados.isEmpty() ->
                    Text("No hay pedidos con este estado", color = KafeGray)

                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(vm.pedidosFiltrados, key = { "${it.uid}_${it.id}" }) { pedido ->
                        val colorEstado = pedido.estado.colorEstado()
                        val actualizando = vm.estaActualizando(pedido)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(KafeCard)
                                .padding(12.dp)
                        ) {
                            Text(
                                pedido.fecha.comoFecha(),
                                color = KafeGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = colorEstado.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = pedido.estado.etiquetaEstado(),
                                    color = colorEstado,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            pedido.items.forEach {
                                Text("${it.cantidad} x ${it.nombre}", color = KafeWhite)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = (if (pedido.tipoEntrega == "DOMICILIO") "A domicilio" else "Recoger en tienda") +
                                    " · " + pedido.metodoPago,
                                color = KafeGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (pedido.tipoEntrega == "DOMICILIO") {
                                Text(
                                    text = "Dirección: ${pedido.direccion.ifBlank { "No disponible" }}",
                                    color = KafeGray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                pedido.total.comoDinero(),
                                color = KafeWhite,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                EstadoPedido.todos.forEach { estado ->
                                    OutlinedButton(
                                        onClick = { vm.cambiarEstado(pedido, estado) },
                                        enabled = !actualizando && pedido.estado != estado,
                                        contentPadding = PaddingValues(horizontal = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            estado.etiquetaEstado(),
                                            color = KafeWhite,
                                            maxLines = 1,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String.etiquetaEstado(): String =
    lowercase().replaceFirstChar { it.uppercase() }

private fun String.colorEstado(): Color = when (this) {
    EstadoPedido.PENDIENTE -> Color(0xFFFFC107)
    EstadoPedido.PREPARADO -> Color(0xFF64B5F6)
    EstadoPedido.ENTREGADO -> Color(0xFF81C784)
    else -> KafeGray
}
