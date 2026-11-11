package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kafecito.app.data.model.EstadoPedido
import com.kafecito.app.ui.components.KafeTopBar
import com.kafecito.app.ui.theme.*
import com.kafecito.app.util.comoDinero
import com.kafecito.app.util.comoFecha
import com.kafecito.app.viewmodel.AdminPedidosViewModel

/**
 * Pedidos del local: todos los pedidos de todos los clientes. Versión base que ya funciona:
 * lista los pedidos y permite cambiar el estado con tres botones.
 *
 * TODO(ADMIN-PEDIDOS) - José: pulir esta pantalla
 *   - Mostrar el estado con color o etiqueta distinta (ahora es solo texto).
 *   - Filtro por estado (chips: Todos / Pendiente / Preparado / Entregado).
 *   - Mostrar la dirección cuando el pedido es a domicilio.
 *   - Si vm.error aparece al cambiar un estado, mostrarlo sin ocultar la lista.
 */
@Composable
fun AdminPedidosScreen(onAtras: () -> Unit) {
    val vm: AdminPedidosViewModel = viewModel()

    Column(modifier = Modifier.fillMaxSize().background(KafeBlack).padding(20.dp)) {
        KafeTopBar(titulo = "PEDIDOS DEL LOCAL", onAtras = onAtras)
        Spacer(modifier = Modifier.height(12.dp))

        when {
            vm.cargando -> CircularProgressIndicator(color = KafeWhite)

            vm.error != null && vm.pedidos.isEmpty() -> Column {
                Text(vm.error ?: "", color = KafeGray)
                TextButton(onClick = { vm.cargar() }) { Text("Reintentar", color = KafeWhite) }
            }

            vm.pedidos.isEmpty() -> Text("Todavía no hay pedidos", color = KafeGray)

            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(vm.pedidos, key = { it.id }) { pedido ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(KafeCard)
                            .padding(12.dp)
                    ) {
                        Text(pedido.fecha.comoFecha(), color = KafeGray, style = MaterialTheme.typography.bodySmall)
                        Text("Estado: ${pedido.estado}", color = KafeWhite, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        pedido.items.forEach { Text("${it.cantidad} x ${it.nombre}", color = KafeWhite) }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = (if (pedido.tipoEntrega == "DOMICILIO") "A domicilio" else "Recoger en tienda") + " · " + pedido.metodoPago,
                            color = KafeGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(pedido.total.comoDinero(), color = KafeWhite, style = MaterialTheme.typography.bodyLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            EstadoPedido.todos.forEach { estado ->
                                OutlinedButton(
                                    onClick = { vm.cambiarEstado(pedido, estado) },
                                    enabled = pedido.estado != estado
                                ) { Text(estado.lowercase().replaceFirstChar { it.uppercase() }, color = KafeWhite) }
                            }
                        }
                    }
                }
            }
        }
    }
}
