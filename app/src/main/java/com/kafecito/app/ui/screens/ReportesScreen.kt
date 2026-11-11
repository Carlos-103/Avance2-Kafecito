package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kafecito.app.data.model.Pedido
import com.kafecito.app.data.repository.PedidoRepository
import com.kafecito.app.ui.components.KafeTopBar
import com.kafecito.app.ui.theme.*
import com.kafecito.app.util.comoDinero
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

/**
 * Reportes de ventas del administrador. Versión base: total vendido y cantidad de pedidos.
 *
 * TODO(REPORTES) - Carlos David
 * Todo se calcula en la app sobre la lista que devuelve PedidoRepository.listarTodos(); no
 * hacen falta consultas nuevas. Agregar (las que alcancen en el tiempo):
 *   - Productos más vendidos:
 *       pedidos.flatMap { it.items }.groupBy { it.nombre }
 *              .mapValues { (_, items) -> items.sumOf { it.cantidad } }
 *              .entries.sortedByDescending { it.value }.take(5)
 *   - Productos con poco stock: ProductoRepository.obtenerProductos().filter { it.stock <= 3 }
 *   - Pedidos por estado (PENDIENTE, PREPARADO, ENTREGADO): groupBy { it.estado }.
 *   - Mover la carga a un ReportesViewModel (parecido a PerfilViewModel) y mostrar mejor el error.
 */
@Composable
fun ReportesScreen(onAtras: () -> Unit) {
    var pedidos by remember { mutableStateOf<List<Pedido>?>(null) }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            pedidos = withTimeout(15.seconds) { PedidoRepository.listarTodos() }
        } catch (e: Exception) {
            error = true
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(KafeBlack).padding(20.dp)) {
        KafeTopBar(titulo = "REPORTES", onAtras = onAtras)
        Spacer(modifier = Modifier.height(12.dp))
        val lista = pedidos
        when {
            error -> Text("No se pudieron cargar los reportes", color = KafeGray)
            lista == null -> CircularProgressIndicator(color = KafeWhite)
            else -> {
                Text("Pedidos realizados", color = KafeGray)
                Text("${lista.size}", color = KafeWhite, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Total vendido", color = KafeGray)
                Text(lista.sumOf { it.total }.comoDinero(), color = KafeWhite, style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}
