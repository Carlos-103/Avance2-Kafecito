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
import com.kafecito.app.data.model.MetodoPago
import com.kafecito.app.ui.components.KafeDarkField
import com.kafecito.app.ui.components.KafeFilaTarjeta
import com.kafecito.app.ui.components.KafeTopBar
import com.kafecito.app.ui.theme.*

/** Pantalla "Métodos de pago". Hoy usa datos de ejemplo en memoria. */
@Composable
fun MetodosPagoScreen(onAtras: () -> Unit) {
    // TODO(PAGOS): cargar con MetodoPagoRepository.listar() en un ViewModel.
    val metodos = remember {
        mutableStateListOf(MetodoPago("1", "Visa", "4242"), MetodoPago("2", "Mastercard", "1111"))
    }
    var marca by remember { mutableStateOf("") }
    var ultimos4 by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(KafeBlack).padding(20.dp)) {
        KafeTopBar(titulo = "MÉTODOS DE PAGO", onAtras = onAtras)

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(metodos, key = { it.id }) { m ->
                KafeFilaTarjeta(titulo = m.marca, subtitulo = m.etiqueta) {
                    // TODO(PAGOS): ademas de quitarlo de la lista, llamar a MetodoPagoRepository.borrar(m.id)
                    IconButton(onClick = { metodos.remove(m) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Borrar", tint = KafeWhite)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Solo guardamos la marca y los últimos 4 dígitos.", color = KafeGray, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        KafeDarkField(label = "Marca (Visa, Mastercard...)", value = marca, onChange = { marca = it })
        Spacer(modifier = Modifier.height(8.dp))
        KafeDarkField(label = "Últimos 4 dígitos", value = ultimos4, onChange = { ultimos4 = it.take(4) }, keyboardType = KeyboardType.Number)
        error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                // TODO(PAGOS): validar (marca no vacia y ultimos4 con exactamente 4 digitos) y poner el mensaje en `error`.
                // Luego guardar con MetodoPagoRepository.agregar(...).
                error = null
                metodos.add(MetodoPago(System.currentTimeMillis().toString(), marca, ultimos4))
                marca = ""; ultimos4 = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = KafeWhite, contentColor = KafeBlack),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("AGREGAR MÉTODO") }
    }
}
