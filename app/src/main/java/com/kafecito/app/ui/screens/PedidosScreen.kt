package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kafecito.app.data.model.Pedido
import com.kafecito.app.data.model.PedidoItem
import com.kafecito.app.ui.components.KafeTopBar
import com.kafecito.app.ui.theme.*
import com.kafecito.app.util.comoDinero
import com.kafecito.app.util.comoFecha
/** Historial de pedidos. Solo lectura: no agregar botones de editar o borrar. */
/** Historial de pedidos. Hoy usa datos de ejemplo. */
@Composable
fun PedidosScreen(
    onAtras: () -> Unit,
    onRepetir: (Pedido) -> Unit
) {
    // TODO(PEDIDOS): reemplazar por PedidoRepository.listar() (mas nuevo primero) en un ViewModel.
    val pedidos = remember {
        val ahora = System.currentTimeMillis()
        listOf(
            Pedido(
                id = "A2", fecha = ahora - 86_400_000L, tipoEntrega = "DOMICILIO", metodoPago = "Visa \u2022\u2022\u2022\u2022 4242",
                items = listOf(PedidoItem(5, "Desayuno tipico", 7.00, 1), PedidoItem(2, "Café Americano", 2.00, 1)),
                subtotal = 9.00, iva = 1.17, total = 10.17
            ),
            Pedido(
                id = "A1", fecha = ahora - 5 * 86_400_000L, tipoEntrega = "RECOGER", metodoPago = "Mastercard \u2022\u2022\u2022\u2022 1111",
                items = listOf(PedidoItem(1, "Capuccino", 2.50, 2)),
                subtotal = 5.00, iva = 0.65, total = 5.65
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(KafeBlack).padding(20.dp)) {
        KafeTopBar(titulo = "MIS PEDIDOS", onAtras = onAtras)

        if (pedidos.isEmpty()) {
            Text("Aún no has hecho pedidos", color = KafeGray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(pedidos, key = { it.id }) { pedido ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(KafeCard)
                            .padding(12.dp)
                    ) {
                        Text(pedido.fecha.comoFecha(), color = KafeGray, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        pedido.items.forEach {
                            Text("${it.cantidad} x ${it.nombre}", color = KafeWhite)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = (if (pedido.tipoEntrega == "DOMICILIO") "A domicilio" else "Recoger en tienda") + " · " + pedido.metodoPago,
                            color = KafeGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pedido.total.comoDinero(), color = KafeWhite, style = MaterialTheme.typography.bodyLarge)
                            TextButton(onClick = { onRepetir(pedido) }) { Text("Repetir", color = KafeWhite) }
                        }
                    }
                }
            }
        }
    }
}
