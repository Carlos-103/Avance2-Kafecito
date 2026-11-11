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
import com.kafecito.app.data.repository.ProductoRepository
import com.kafecito.app.ui.components.KafeTopBar
import com.kafecito.app.ui.theme.*
import com.kafecito.app.util.comoDinero

/**
 * Gestión de productos del administrador. Versión base: solo lista los productos
 * con su precio y stock.
 *
 * TODO(ADMIN-PRODUCTOS) - Ricardo
 * Modelo a copiar y adaptar: DireccionesScreen.kt (lista + formulario con validación +
 * editar + borrar + estados de carga y error + timeouts).
 *   1. Tocar un producto para editarlo y un botón "Nuevo producto".
 *   2. Formulario: nombre, descripción, precio, stock y categoría (categorías en ProductoRepository.categorias).
 *      Validar: nombre no vacío, precio > 0 y stock >= 0 (ver util/Validaciones.kt).
 *   3. Usar AdminProductosViewModel (ya hecho): vm.productos, vm.guardar(producto) { cerrar },
 *      vm.borrar(id), vm.siguienteId(), vm.guardando y vm.error. No llamar a Firestore desde la pantalla.
 * Depende de TODO(CATALOGO-FS) para ver los cambios reflejados en Home.
 */
@Composable
fun AdminProductosScreen(onAtras: () -> Unit) {
    val productos = ProductoRepository.obtenerProductos()

    Column(modifier = Modifier.fillMaxSize().background(KafeBlack).padding(20.dp)) {
        KafeTopBar(titulo = "PRODUCTOS", onAtras = onAtras)
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(productos, key = { it.id }) { p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KafeCard)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(p.nombre, color = KafeWhite, style = MaterialTheme.typography.bodyLarge)
                        Text(p.categoria, color = KafeGray, style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(p.precio.comoDinero(), color = KafeWhite)
                        Text("Stock: ${p.stock}", color = KafeGray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
