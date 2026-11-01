package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kafecito.app.data.model.Direccion
import com.kafecito.app.data.model.Pedido
import com.kafecito.app.data.model.aPedidoItems
import com.kafecito.app.ui.components.KafeTopBar
import com.kafecito.app.ui.theme.*
import com.kafecito.app.util.comoDinero
import com.kafecito.app.viewmodel.CarritoViewModel
import com.kafecito.app.viewmodel.PagarViewModel

/** ID fijo de la opción "Efectivo en tienda". No se guarda en Firestore. */
private const val ID_EFECTIVO = "EFECTIVO"
private const val TEXTO_EFECTIVO = "Efectivo en tienda"

/**
 * Pantalla de pago que permite seleccionar el tipo de entrega,
 * el metodo de pago y confirmar el pedido.
 */
@Composable
fun PagarScreen(
    carritoVM: CarritoViewModel,
    onAtras: () -> Unit,
    onPedidoCreado: () -> Unit
) {
    val vm: PagarViewModel = viewModel()

    var entrega by remember { mutableStateOf("RECOGER") }
    var direccionId by remember { mutableStateOf<String?>(null) }
    // Por defecto se selecciona "Efectivo en tienda", incluso si no hay tarjetas guardadas.
    var metodoId by remember { mutableStateOf(ID_EFECTIVO) }

    // Utiliza la dirección seleccionada o, por defecto, la primera guardada.
    val direccionSel = vm.direcciones.firstOrNull { it.id == direccionId } ?: vm.direcciones.firstOrNull()
    // Si no hay una tarjeta seleccionada, se utiliza el pago en efectivo.
    val metodoSel = vm.metodos.firstOrNull { it.id == metodoId }
    val textoMetodo = metodoSel?.etiqueta ?: TEXTO_EFECTIVO

    val sinDireccionParaDomicilio = entrega == "DOMICILIO" && direccionSel == null
    val puedeConfirmar = carritoVM.items.isNotEmpty() && !vm.procesando && !vm.cargando && !sinDireccionParaDomicilio

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KafeBlack)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        KafeTopBar(titulo = "PAGAR", onAtras = onAtras)

        Text("Entrega", color = KafeGray)
        OpcionRadio(texto = "Recoger en tienda", seleccionada = entrega == "RECOGER") { entrega = "RECOGER" }
        OpcionRadio(texto = "A domicilio", seleccionada = entrega == "DOMICILIO") { entrega = "DOMICILIO" }

        if (entrega == "DOMICILIO") {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Dirección de entrega", color = KafeGray)
            if (vm.direcciones.isEmpty()) {
                Text(
                    text = "No tienes direcciones guardadas. Agrégalas en Perfil → Mis direcciones.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                vm.direcciones.forEach { d ->
                    OpcionRadio(texto = textoDireccion(d), seleccionada = direccionSel?.id == d.id) { direccionId = d.id }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Método de pago", color = KafeGray)
        OpcionRadio(texto = TEXTO_EFECTIVO, seleccionada = metodoSel == null) { metodoId = ID_EFECTIVO }
        vm.metodos.forEach { m ->
            OpcionRadio(texto = m.etiqueta, seleccionada = metodoSel?.id == m.id) { metodoId = m.id }
        }
        if (!vm.cargando && vm.metodos.isEmpty()) {
            Text(
                text = "No tienes tarjetas guardadas. Puedes agregarlas en Perfil → Métodos de pago.",
                color = KafeGray,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Resumen", color = KafeGray)
        FilaTotal("Subtotal", carritoVM.subtotal)
        FilaTotal("IVA (13%)", carritoVM.iva)
        FilaTotal("TOTAL", carritoVM.total, destacado = true)

        vm.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                // Se toma una copia de los datos actuales del carrito para crear el pedido.
                val pedido = Pedido(
                    fecha = System.currentTimeMillis(),
                    tipoEntrega = entrega,
                    direccion = if (entrega == "DOMICILIO") direccionSel?.let { textoDireccion(it) }.orEmpty() else "",
                    metodoPago = textoMetodo,
                    items = carritoVM.items.aPedidoItems(),
                    subtotal = carritoVM.subtotal,
                    iva = carritoVM.iva,
                    total = carritoVM.total
                )
                // Solo si el pedido se guarda correctamente, se vacía el carrito y se abre "Mis pedidos".
                vm.confirmar(pedido) {
                    carritoVM.vaciar()
                    onPedidoCreado()
                }
            },
            enabled = puedeConfirmar,
            colors = ButtonDefaults.buttonColors(containerColor = KafeWhite, contentColor = KafeBlack),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text(if (vm.procesando) "CONFIRMANDO..." else "CONFIRMAR PEDIDO") }
    }
}

/** Construye el texto de una dirección con su alias, ubicación y referencia. */
private fun textoDireccion(d: Direccion): String {
    val base = "${d.alias}: ${d.calle}, ${d.ciudad}"
    return if (d.referencia.isBlank()) base else "$base (${d.referencia})"
}

@Composable
private fun OpcionRadio(texto: String, seleccionada: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = seleccionada,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = KafeWhite, unselectedColor = KafeGray)
        )
        Text(text = texto, color = KafeWhite)
    }
}

@Composable
private fun FilaTotal(etiqueta: String, monto: Double, destacado: Boolean = false) {
    val peso = if (destacado) FontWeight.Bold else FontWeight.Normal
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = etiqueta, color = KafeWhite, fontWeight = peso)
        Text(text = monto.comoDinero(), color = KafeWhite, fontWeight = peso)
    }
}