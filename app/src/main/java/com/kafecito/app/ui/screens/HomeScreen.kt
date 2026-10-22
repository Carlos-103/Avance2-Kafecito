package com.kafecito.app.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kafecito.app.data.model.Producto
import com.kafecito.app.data.repository.AuthRepository
import com.kafecito.app.data.repository.KafecitoRepository
import com.kafecito.app.ui.components.KafeHeader
import com.kafecito.app.ui.theme.*
import com.kafecito.app.ui.components.ProductoImagen


/**
 * Pantalla principal: saludo, buscador, categorías y grid de productos.
 *  - Tocar una tarjeta abre el detalle del producto.
 *  - Tocar el "+" agrega 1 unidad al carrito (CarritoViewModel.agregar).
 */
@Composable
fun HomeScreen(
// La pantalla comunica los eventos al componente padre,
// que se encarga de gestionar la navegación.
    onAbrirCarrito: () -> Unit,
    onAbrirDetalle: (Int) -> Unit,
    onAgregar: (Producto) -> Unit
) {
    // Cuando el estado cambia, Compose actualiza la interfaz que depende de él.
    var categoriaSeleccionada by remember { mutableStateOf(KafecitoRepository.categorias.first()) }
    var busqueda by remember { mutableStateOf("") }
// Se actualiza automáticamente cuando cambia la categoría o el texto de búsqueda.
    val productos = KafecitoRepository.obtenerProductos(categoriaSeleccionada)
        .filter { it.nombre.contains(busqueda, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KafeBlack)
    ) {
        KafeHeader {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¡Hola! ${AuthRepository.nombreActual()}",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onAbrirCarrito) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KafecitoRepository.categorias.forEach { categoria ->
                CategoriaChip(
                    texto = categoria,
                    seleccionada = categoria == categoriaSeleccionada,
                    onClick = { categoriaSeleccionada = categoria }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
// Muestra los productos de forma eficiente, cargando los elementos según se necesitan.
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(productos) { producto ->
                ProductoCard(
                    producto = producto,
                    onClick = { onAbrirDetalle(producto.id) },
                    onAgregar = { onAgregar(producto) }
                )
            }
        }
    }
}
// Botón de categoría que indica cuál está seleccionada y permite cambiarla.
@Composable
private fun CategoriaChip(texto: String, seleccionada: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (seleccionada) KafeGray else KafeChip)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // El color del texto cambia según la categoría seleccionada.
        Text(
            text = texto,
            color = if (seleccionada) KafeWhite else KafeBlack,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
// Tarjeta que muestra la información del producto y permite abrirlo o agregarlo al carrito.
@Composable
private fun ProductoCard(
    producto: Producto,
    onClick: () -> Unit,
    onAgregar: () -> Unit
) {
    Column(
        // El orden importa: primero la forma, luego el fondo, el clic y el espaciado.
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(KafeSurface)
            .clickable { onClick() }
            .padding(10.dp)
    ) {
// Muestra la imagen del producto con un tamaño y bordes redondeados.
        ProductoImagen(
            imagen = producto.imagen,
            descripcion = producto.nombre,
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = producto.nombre, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
        Text(
            text = producto.descripcion,
            style = MaterialTheme.typography.bodySmall,
            color = KafeGray,
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Muestra el precio del producto con dos decimales.
            Text(text = "$${"%.2f".format(producto.precio)}", style = MaterialTheme.typography.bodyLarge)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(KafeBlack)
                    // Este clic agrega el producto sin abrir su detalle.
                    .clickable { onAgregar() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Agregar al carrito",
                    tint = KafeWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
