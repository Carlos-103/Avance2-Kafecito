package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kafecito.app.data.model.Pedido
import com.kafecito.app.ui.components.KafeTopBar
import com.kafecito.app.ui.theme.*
import com.kafecito.app.util.comoDinero
import com.kafecito.app.util.comoFecha
import com.kafecito.app.viewmodel.PedidosViewModel
/**
 * Muestra el historial de pedidos del usuario desde Firestore,
 * ordenado del más reciente al más antiguo.
 *
 * Permite consultar los detalles y volver a agregar un pedido al carrito,
 * sin modificar ni eliminar pedidos existentes.
 */
@Composable
fun PedidosScreen(
    onAtras: () -> Unit,
    onRepetir: (Pedido) -> Unit
) {
    val vm: PedidosViewModel = viewModel()
    val pedidos = vm.pedidos

    Column(modifier = Modifier.fillMaxSize().background(KafeBlack).padding(20.dp)) {
        KafeTopBar(titulo = "MIS PEDIDOS", onAtras = onAtras)

        when {
            vm.cargando -> CircularProgressIndicator(color = KafeWhite)

            vm.error != null -> Column {
                Text(vm.error ?: "", color = KafeGray)
                TextButton(onClick = { vm.cargar() }) { Text("Reintentar", color = KafeWhite) }
            }

            pedidos.isEmpty() -> Text("Aún no has hecho pedidos", color = KafeGray)

            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
