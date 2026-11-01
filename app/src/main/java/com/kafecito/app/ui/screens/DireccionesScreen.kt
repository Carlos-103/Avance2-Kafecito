package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kafecito.app.data.model.Direccion
import com.kafecito.app.data.repository.DireccionRepository
import com.kafecito.app.ui.components.KafeDarkField
import com.kafecito.app.ui.components.KafeFilaTarjeta
import com.kafecito.app.ui.components.KafeTopBar
import com.kafecito.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

/**
 * Muestra las direcciones guardadas del usuario y permite agregarlas,
 * editarlas y eliminarlas mediante Firestore.
 */
@Composable
fun DireccionesScreen(onAtras: () -> Unit) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val direcciones = remember { mutableStateListOf<Direccion>() }
    var cargando by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }

    // ID de la dirección que se está editando. Si es null, se agrega una nueva.
    var editandoId by remember { mutableStateOf<String?>(null) }

    var alias by remember { mutableStateOf("") }
    var calle by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var errores by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val enModoEdicion = editandoId != null

    fun limpiarFormulario() {
        editandoId = null
        alias = ""; calle = ""; ciudad = ""
        errores = emptyMap()
    }

    fun iniciarEdicion(d: Direccion) {
        editandoId = d.id
        alias = d.alias
        calle = d.calle
        ciudad = d.ciudad
        errores = emptyMap()
    }

    // Carga las direcciones guardadas del usuario al abrir la pantalla.
    LaunchedEffect(Unit) {
        var mensajeError: String? = null
        try {
            direcciones.clear()
            direcciones.addAll(withTimeout(15.seconds) { DireccionRepository.listar() })
        } catch (e: Exception) {
            mensajeError = "No se pudieron cargar tus direcciones"
        }
        cargando = false
        mensajeError?.let { snackbarHostState.showSnackbar(it) }
    }

    fun guardar() {
        val idEditando = editandoId
        val nuevosErrores = validarDireccion(alias, calle, ciudad, direcciones, idEditando)
        errores = nuevosErrores
        if (nuevosErrores.isNotEmpty()) return

        guardando = true
        scope.launch {
            var mensajeError: String? = null
            try {
                if (idEditando == null) {
                    val nueva = withTimeout(15.seconds) {
                        DireccionRepository.agregar(alias.trim(), calle.trim(), ciudad.trim()) }
                    direcciones.add(nueva)
                } else {
                     val actualizada = withTimeout(15.seconds) {
                        DireccionRepository.actualizar(
                            id = idEditando,
                            alias = alias.trim(),
                            calle = calle.trim(),
                            ciudad = ciudad.trim()
                        )
                    }
                    val indice = direcciones.indexOfFirst { it.id == idEditando }
                    if (indice >= 0) direcciones[indice] = actualizada
                }
                limpiarFormulario()
            } catch (e: Exception) {
                mensajeError = if (idEditando == null) "No se pudo guardar la dirección"
                else "No se pudo actualizar la dirección"
            }
            guardando = false
            mensajeError?.let { snackbarHostState.showSnackbar(it) }
        }
    }

    fun borrar(d: Direccion) {
        scope.launch {
            var mensajeError: String? = null
            try {
                withTimeout(15.seconds) { DireccionRepository.borrar(d.id) }
                direcciones.removeAll { it.id == d.id }
                // Si se elimina la dirección que se estaba editando, se limpia el formulario.
                if (editandoId == d.id) limpiarFormulario()
            } catch (e: Exception) {
                mensajeError = "No se pudo borrar la dirección"
            }
            mensajeError?.let { snackbarHostState.showSnackbar(it) }
        }
    }

    Scaffold(
        containerColor = KafeBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            KafeTopBar(titulo = "MIS DIRECCIONES", onAtras = onAtras)

            when {
                cargando -> Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = KafeWhite)
                }

                direcciones.isEmpty() -> Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aún no tienes direcciones guardadas", color = KafeGray)
                }

                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(direcciones, key = { it.id }) { d ->
                        KafeFilaTarjeta(
                            titulo = d.alias,
                            subtitulo = "${d.calle}, ${d.ciudad}"
                        ) {
                            Row {
                                IconButton(onClick = { iniciarEdicion(d) }) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Editar dirección ${d.alias}",
                                        tint = KafeWhite
                                    )
                                }
                                IconButton(onClick = { borrar(d) }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Borrar dirección ${d.alias}",
                                        tint = KafeWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (enModoEdicion) "EDITAR DIRECCIÓN" else "NUEVA DIRECCIÓN",
                color = KafeWhite,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))

            KafeDarkField(
                label = "Alias (Casa, Trabajo...)",
                value = alias,
                onChange = { alias = it; errores = errores - "alias" }
            )
            errores["alias"]?.let { ErrorTexto(it) }

            Spacer(modifier = Modifier.height(8.dp))

            KafeDarkField(
                label = "Calle y número",
                value = calle,
                onChange = { calle = it; errores = errores - "calle" }
            )
            errores["calle"]?.let { ErrorTexto(it) }

            Spacer(modifier = Modifier.height(8.dp))

            KafeDarkField(
                label = "Ciudad",
                value = ciudad,
                onChange = { ciudad = it; errores = errores - "ciudad" }
            )
            errores["ciudad"]?.let { ErrorTexto(it) }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { guardar() },
                enabled = !guardando && !cargando,
                colors = ButtonDefaults.buttonColors(containerColor = KafeWhite, contentColor = KafeBlack),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    when {
                        guardando -> "GUARDANDO..."
                        enModoEdicion -> "GUARDAR CAMBIOS"
                        else -> "AGREGAR DIRECCIÓN"
                    }
                )
            }

            if (enModoEdicion) {
                TextButton(
                    onClick = { limpiarFormulario() },
                    enabled = !guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar edición", color = KafeGray)
                }
            }
        }
    }
}

@Composable
private fun ErrorTexto(mensaje: String) {
    Text(
        text = mensaje,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall
    )
}

/**
 * Valida los campos del formulario y devuelve los errores encontrados.
 *
 * La dirección que se está editando se excluye de la comprobación
 * de alias duplicados para permitir conservar su alias actual.
 */
private fun validarDireccion(
    alias: String,
    calle: String,
    ciudad: String,
    existentes: List<Direccion>,
    idEditando: String?
): Map<String, String> {
    val errores = mutableMapOf<String, String>()
    val a = alias.trim()
    val c = calle.trim()
    val ci = ciudad.trim()

    when {
        a.isEmpty() -> errores["alias"] = "Escribe un alias"
        a.length > 30 -> errores["alias"] = "Máximo 30 caracteres"
        existentes.any { it.id != idEditando && it.alias.equals(a, ignoreCase = true) } ->
            errores["alias"] = "Ya tienes una dirección con ese alias"
    }

    when {
        c.isEmpty() -> errores["calle"] = "Escribe la calle y el número"
        c.length < 5 -> errores["calle"] = "La dirección es demasiado corta"
        c.length > 100 -> errores["calle"] = "Máximo 100 caracteres"
    }

    when {
        ci.isEmpty() -> errores["ciudad"] = "Escribe la ciudad"
        ci.length > 50 -> errores["ciudad"] = "Máximo 50 caracteres"
    }

    return errores
}
