package com.kafecito.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kafecito.app.data.model.Producto
import com.kafecito.app.data.repository.ProductoRepository
import com.kafecito.app.ui.components.KafeDarkField
import com.kafecito.app.ui.components.KafeFilaTarjeta
import com.kafecito.app.ui.components.KafeTopBar
import com.kafecito.app.ui.theme.*
import com.kafecito.app.util.Validaciones
import com.kafecito.app.util.comoDinero
import com.kafecito.app.viewmodel.AdminProductosViewModel

/**
 * Permite al administrador consultar, crear, editar y eliminar productos.
 * La pantalla utiliza AdminProductosViewModel para gestionar las operaciones,
 * mientras que las reglas de Firestore controlan el acceso a los datos.
 */
@Composable
fun AdminProductosScreen(onAtras: () -> Unit) {
    val vm: AdminProductosViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado del formulario. rememberSaveable evita perder lo escrito al rotar la pantalla.
    var formAbierto by rememberSaveable { mutableStateOf(false) }
    // ID del producto que se edita. Si es null, el formulario crea uno nuevo.
    var editandoId by rememberSaveable { mutableStateOf<Int?>(null) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var precio by rememberSaveable { mutableStateOf("") }
    var stock by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf(ProductoRepository.categorias.first()) }
    var errores by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    // Producto pendiente de confirmar su borrado.
    var porBorrar by remember { mutableStateOf<Producto?>(null) }
    var imagenUrl by rememberSaveable { mutableStateOf("") }


    fun cerrarFormulario() {
        formAbierto = false
        editandoId = null
        nombre = ""; descripcion = ""; precio = ""; stock = "";imagenUrl=""
        categoria = ProductoRepository.categorias.first()
        errores = emptyMap()
    }

    fun abrirNuevo() {
        cerrarFormulario()
        formAbierto = true
    }

    fun abrirEdicion(p: Producto) {
        editandoId = p.id
        nombre = p.nombre
        descripcion = p.descripcion
        precio = p.precio.toString()
        stock = p.stock.toString()
        categoria = p.categoria
        imagenUrl = p.imagenUrl.orEmpty()
        errores = emptyMap()
        formAbierto = true
    }

    fun guardar() {
        if (vm.guardando) return
        val urlLimpia = imagenUrl.trim()
        val nuevosErrores = Validaciones.validarProducto(nombre, precio, stock).toMutableMap()
        if (urlLimpia.isNotEmpty() && !urlLimpia.startsWith("https://")) {
            nuevosErrores["imagenUrl"] = "El enlace debe empezar con https://"
        }
        errores = nuevosErrores
        if (nuevosErrores.isNotEmpty()) return
        // Al editar se conserva la imagen que ya tenía el producto.
        val existente = vm.productos.firstOrNull { it.id == editandoId }
        val producto = Producto(
            id = existente?.id ?: vm.siguienteId(),
            nombre = nombre.trim(),
            descripcion = descripcion.trim(),
            precio = precio.trim().replace(',', '.').toDouble(),
            stock = stock.trim().toInt(),
            categoria = categoria,
            imagen = existente?.imagen,
            imagenUrl = urlLimpia.ifBlank { null }
        )
        // El formulario solo se cierra si el guardado fue exitoso.
        vm.guardar(producto) { cerrarFormulario() }
    }

    // Muestra los errores del ViewModel como mensaje temporal sin ocultar la pantalla.
    LaunchedEffect(vm.error) {
        vm.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.limpiarError()
        }
    }

    // El botón "atrás" del sistema cierra el formulario en lugar de salir de la pantalla.
    BackHandler(enabled = formAbierto && !vm.guardando) { cerrarFormulario() }

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
            KafeTopBar(
                titulo = when {
                    !formAbierto -> "PRODUCTOS"
                    editandoId == null -> "NUEVO PRODUCTO"
                    else -> "EDITAR PRODUCTO"
                },
                onAtras = { if (formAbierto) cerrarFormulario() else onAtras() }
            )

            if (!formAbierto) {
                // ---- Lista de productos ----
                if (vm.productos.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Aún no hay productos", color = KafeGray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(vm.productos, key = { it.id }) { p ->
                            val detalle = if (p.stock <= 0) "Agotado" else "Stock: ${p.stock}"
                            KafeFilaTarjeta(
                                titulo = p.nombre,
                                subtitulo = "${p.categoria} · ${p.precio.comoDinero()} · $detalle"
                            ) {
                                Row {
                                    IconButton(onClick = { abrirEdicion(p) }, enabled = !vm.guardando) {
                                        Icon(
                                            Icons.Filled.Edit,
                                            contentDescription = "Editar ${p.nombre}",
                                            tint = KafeWhite
                                        )
                                    }
                                    IconButton(onClick = { porBorrar = p }, enabled = !vm.guardando) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Borrar ${p.nombre}",
                                            tint = KafeWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { abrirNuevo() },
                    enabled = !vm.guardando,
                    colors = ButtonDefaults.buttonColors(containerColor = KafeWhite, contentColor = KafeBlack),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("NUEVO PRODUCTO") }
            } else {
                // ---- Formulario de creación / edición ----
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    KafeDarkField(
                        label = "Nombre",
                        value = nombre,
                        onChange = { nombre = it.take(60); errores = errores - "nombre" }
                    )
                    errores["nombre"]?.let { ErrorCampo(it) }

                    Spacer(modifier = Modifier.height(8.dp))
                    KafeDarkField(
                        label = "Descripción",
                        value = descripcion,
                        onChange = { descripcion = it.take(120) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    KafeDarkField(
                        label = "URL de la imagen",
                        value = imagenUrl,
                        onChange = { imagenUrl = it.take(300); errores = errores - "imagenUrl" },
                        keyboardType = KeyboardType.Uri
                    )
                    errores["imagenUrl"]?.let { ErrorCampo(it) }

                    Spacer(modifier = Modifier.height(8.dp))
                    KafeDarkField(
                        label = "Precio (USD)",
                        value = precio,
                        onChange = {
                            precio = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(8)
                            errores = errores - "precio"
                        },
                        keyboardType = KeyboardType.Decimal
                    )
                    errores["precio"]?.let { ErrorCampo(it) }

                    Spacer(modifier = Modifier.height(8.dp))
                    KafeDarkField(
                        label = "Stock (unidades)",
                        value = stock,
                        onChange = {
                            stock = it.filter { c -> c.isDigit() }.take(6)
                            errores = errores - "stock"
                        },
                        keyboardType = KeyboardType.Number
                    )
                    errores["stock"]?.let { ErrorCampo(it) }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Categoría", color = KafeGray, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProductoRepository.categorias.forEach { c ->
                            ChipCategoria(
                                texto = c,
                                seleccionada = c == categoria,
                                onClick = { categoria = c }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { guardar() },
                        enabled = !vm.guardando,
                        colors = ButtonDefaults.buttonColors(containerColor = KafeWhite, contentColor = KafeBlack),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            when {
                                vm.guardando -> "GUARDANDO..."
                                editandoId == null -> "CREAR PRODUCTO"
                                else -> "GUARDAR CAMBIOS"
                            }
                        )
                    }
                    TextButton(
                        onClick = { cerrarFormulario() },
                        enabled = !vm.guardando,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cancelar", color = KafeGray) }
                }
            }
        }
    }

    // Confirmación antes de borrar un producto.
    porBorrar?.let { p ->
        AlertDialog(
            onDismissRequest = { porBorrar = null },
            containerColor = KafeCard,
            titleContentColor = KafeWhite,
            textContentColor = KafeGray,
            title = { Text("Borrar producto") },
            text = { Text("¿Quieres borrar \"${p.nombre}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.borrar(p.id)
                    porBorrar = null
                }) { Text("Borrar", color = KafeWhite) }
            },
            dismissButton = {
                TextButton(onClick = { porBorrar = null }) { Text("Cancelar", color = KafeGray) }
            }
        )
    }
}

/** Botón de selección de categoría dentro del formulario. */
@Composable
private fun ChipCategoria(texto: String, seleccionada: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (seleccionada) KafeGray else KafeChip)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = texto,
            color = if (seleccionada) KafeWhite else KafeBlack,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
/** Muestra un mensaje de validación debajo del campo correspondiente. */
@Composable
private fun ErrorCampo(mensaje: String) {
    Text(
        text = mensaje,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall
    )
}