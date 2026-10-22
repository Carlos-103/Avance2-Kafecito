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
import androidx.compose.ui.unit.dp
import com.kafecito.app.data.model.Direccion
import com.kafecito.app.ui.components.KafeDarkField
import com.kafecito.app.ui.components.KafeFilaTarjeta
import com.kafecito.app.ui.components.KafeTopBar
import com.kafecito.app.ui.theme.*

/** Pantalla "Mis direcciones". Hoy usa datos de ejemplo en memoria. */
@Composable
fun DireccionesScreen(onAtras: () -> Unit) {
    // TODO(DIRECCIONES): cargar con DireccionRepository.listar() en un ViewModel.
    val direcciones = remember {
        mutableStateListOf(
            Direccion("1", "Casa", "Col. Escalón, calle 5", "San Salvador"),
            Direccion("2", "Trabajo", "Blvd. Los Próceres 12", "San Salvador")
        )
    }
    var alias by remember { mutableStateOf("") }
    var calle by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(KafeBlack).padding(20.dp)) {
        KafeTopBar(titulo = "MIS DIRECCIONES", onAtras = onAtras)

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(direcciones, key = { it.id }) { d ->
                KafeFilaTarjeta(titulo = d.alias, subtitulo = "${d.calle}, ${d.ciudad}") {
                    // TODO(DIRECCIONES): ademas de quitarla de la lista, llamar a DireccionRepository.borrar(d.id)
                    IconButton(onClick = { direcciones.remove(d) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Borrar", tint = KafeWhite)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        KafeDarkField(label = "Alias (Casa, Trabajo...)", value = alias, onChange = { alias = it })
        Spacer(modifier = Modifier.height(8.dp))
        KafeDarkField(label = "Calle y número", value = calle, onChange = { calle = it })
        Spacer(modifier = Modifier.height(8.dp))
        KafeDarkField(label = "Ciudad", value = ciudad, onChange = { ciudad = it })
        error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                // TODO(DIRECCIONES): validar el formulario (campos no vacios) y poner el mensaje en `error`.
                // Luego guardar con DireccionRepository.agregar(...) en vez de solo la lista local.
                error = null
                direcciones.add(Direccion(System.currentTimeMillis().toString(), alias, calle, ciudad))
                alias = ""; calle = ""; ciudad = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = KafeWhite, contentColor = KafeBlack),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("AGREGAR DIRECCIÓN") }
    }
}
