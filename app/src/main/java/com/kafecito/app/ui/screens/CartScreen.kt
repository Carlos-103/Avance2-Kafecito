package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kafecito.app.data.model.ItemCarrito
import com.kafecito.app.ui.theme.*
import com.kafecito.app.viewmodel.CarritoViewModel
import com.kafecito.app.ui.components.ProductoImagen

/**
 * Pantalla del carrito: muestra los productos agregados, con botones para
 * sumar, restar y quitar, y el resumen del pedido.
 */
@Composable
fun CartScreen(
    carritoVM: CarritoViewModel,
    onAtras: () -> Unit,
    onIrAPagar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KafeBlack)
            .padding(20.dp)
    ) {
        // Encabezado con el botón para regresar al Home.
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onAtras) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = KafeWhite)
            }
            Text(text = "MI PEDIDO", color = KafeWhite, style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(12.dp))
// Si no hay productos, muestra un mensaje; de lo contrario, presenta la lista.
        if (carritoVM.items.isEmpty()) {
            Text(text = "Tu pedido está vacío", color = KafeGray, modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(carritoVM.items, key = { it.producto.id }) { item ->
                    CartItem(
                        item = item,
                        onCambiarCantidad = { nueva -> carritoVM.cambiarCantidad(item.producto.id, nueva) },
                        onQuitar = { carritoVM.quitar(item.producto.id) }
                    )
                }
            }
        }
// Permite vaciar el carrito cuando contiene productos.
        if (carritoVM.items.isNotEmpty()) {
            TextButton(onClick = { carritoVM.vaciar() }, modifier = Modifier.align(Alignment.End)) {
                Text("Vaciar carrito", color = KafeGray)
            }
        }
        // Muestra el resumen de precios; los cálculos dependen del CarritoViewModel.
        TotalRow("Subtotal", carritoVM.subtotal)
        TotalRow("IVA (13%)", carritoVM.iva)
        Spacer(modifier = Modifier.height(4.dp))
        TotalRow("TOTAL", carritoVM.total, destacado = true)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onIrAPagar,
            enabled = carritoVM.items.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = KafeWhite, contentColor = KafeBlack),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("Ir a Pagar") }
    }
}
/**
 * Elemento individual del carrito que muestra la imagen y el precio del producto.
 */
@Composable
private fun CartItem(
    item: ItemCarrito,
    onCambiarCantidad: (Int) -> Unit,
    onQuitar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KafeCard)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Muestra la imagen del producto con bordes redondeados.
        ProductoImagen(
            imagen = item.producto.imagen,
            descripcion = item.producto.nombre,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.producto.nombre,
                color = KafeWhite,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )
            // Los botones permiten cambiar la cantidad o quitar el producto.
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onCambiarCantidad(item.cantidad - 1) }) {
                    Icon(Icons.Filled.Remove, contentDescription = "Restar", tint = KafeWhite)
                }
                Text(text = item.cantidad.toString(), color = KafeWhite)
                IconButton(
                    onClick = { onCambiarCantidad(item.cantidad + 1) },
                    enabled = item.cantidad < item.producto.stock
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Sumar",
                        tint = if (item.cantidad < item.producto.stock) KafeWhite else KafeGray
                    )
                }
                IconButton(onClick = onQuitar) {
                    Icon(Icons.Filled.Delete, contentDescription = "Quitar", tint = KafeWhite)
                }
            }
        }

        Text(
            text = "$${"%.2f".format(item.totalLinea)}",
            color = KafeWhite,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
// Fila reutilizable para mostrar un concepto del pedido y su importe.
@Composable
private fun TotalRow(etiqueta: String, monto: Double, destacado: Boolean = false) {
    // El total final se muestra en negrita para distinguirlo del resto.
    val peso = if (destacado) FontWeight.Bold else FontWeight.Normal
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = etiqueta, color = KafeWhite, fontWeight = peso)
        Text(text = "$${"%.2f".format(monto)}", color = KafeWhite, fontWeight = peso)
    }
}
