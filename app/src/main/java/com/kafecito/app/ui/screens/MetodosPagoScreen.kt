package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kafecito.app.ui.components.KafeDarkField
import com.kafecito.app.ui.components.KafeFilaTarjeta
import com.kafecito.app.ui.components.KafeTopBar
import com.kafecito.app.ui.theme.*
import com.kafecito.app.viewmodel.MetodosPagoViewModel

/**
 * Muestra los métodos de pago guardados y permite agregar o eliminar tarjetas.
 * Los datos se almacenan en Firestore por usuario.
 */
@Composable
fun MetodosPagoScreen(onAtras: () -> Unit) {
    val vm: MetodosPagoViewModel = viewModel()
    var marca by remember { mutableStateOf("") }
    var ultimos4 by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(KafeBlack).padding(20.dp)) {
        KafeTopBar(titulo = "MÉTODOS DE PAGO", onAtras = onAtras)

        if (vm.metodos.isEmpty()) {
            Text(
                text = "Aún no tienes métodos de pago guardados",
                color = KafeGray,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(vm.metodos, key = { it.id }) { m ->
                    KafeFilaTarjeta(titulo = m.marca, subtitulo = m.etiqueta) {
                        IconButton(onClick = { vm.borrar(m.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Borrar", tint = KafeWhite)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Solo guardamos la marca y los últimos 4 dígitos.", color = KafeGray, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        KafeDarkField(label = "Marca (Visa, Mastercard...)", value = marca, onChange = { marca = it })
        Spacer(modifier = Modifier.height(8.dp))
        // Limita la entrada a cuatro caracteres numéricos.
        KafeDarkField(
            label = "Últimos 4 dígitos",
            value = ultimos4,
            onChange = { ultimos4 = it.filter { c -> c.isDigit() }.take(4) },
            keyboardType = KeyboardType.Number
        )
        vm.error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                // Si la validación es correcta, se agrega el metodo y se limpian los campos.
                if (vm.agregar(marca, ultimos4)) {
                    marca = ""
                    ultimos4 = ""
                }
            },
            enabled = !vm.guardando,
            colors = ButtonDefaults.buttonColors(containerColor = KafeWhite, contentColor = KafeBlack),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text(if (vm.guardando) "GUARDANDO..." else "AGREGAR MÉTODO") }
    }
}
