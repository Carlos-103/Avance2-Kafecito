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
import com.kafecito.app.data.model.MetodoPago
import com.kafecito.app.ui.components.KafeTopBar
import com.kafecito.app.ui.theme.*
import com.kafecito.app.util.comoDinero
import com.kafecito.app.viewmodel.CarritoViewModel

/** Pantalla de pago: tipo de entrega, método de pago y resumen del pedido. */
@Composable
fun PagarScreen(
    carritoVM: CarritoViewModel,
    onAtras: () -> Unit,
    onPedidoCreado: () -> Unit
) {
    var entrega by remember { mutableStateOf("RECOGER") }
    // TODO(CHECKOUT): reemplazar por MetodoPagoRepository.listar() (y si es DOMICILIO, elegir una de DireccionRepository.listar()).
    val metodos = remember { listOf(MetodoPago("1", "Visa", "4242"), MetodoPago("2", "Mastercard", "1111")) }
    var metodoId by remember { mutableStateOf(metodos.first().id) }

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

        Spacer(modifier = Modifier.height(16.dp))
        Text("Método de pago", color = KafeGray)
        metodos.forEach { m ->
            OpcionRadio(texto = m.etiqueta, seleccionada = metodoId == m.id) { metodoId = m.id }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Resumen", color = KafeGray)
        FilaTotal("Subtotal", carritoVM.subtotal)
        FilaTotal("IVA (13%)", carritoVM.iva)
        FilaTotal("TOTAL", carritoVM.total, destacado = true)

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                // TODO(CHECKOUT): al confirmar:
                //  1. armar un Pedido (items = carritoVM.items.aPedidoItems(), subtotal, iva, total, tipoEntrega, metodoPago, fecha = System.currentTimeMillis())
                //  2. PedidoRepository.crear(pedido)   (en una corrutina, desde un ViewModel)
                //  3. carritoVM.vaciar()
                //  4. onPedidoCreado()
            },
            enabled = carritoVM.items.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = KafeWhite, contentColor = KafeBlack),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("CONFIRMAR PEDIDO") }
    }
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
