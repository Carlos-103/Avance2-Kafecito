package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kafecito.app.data.model.Producto
import com.kafecito.app.data.repository.FavoritosRepository
import com.kafecito.app.data.repository.KafecitoRepository
import com.kafecito.app.ui.components.KafeHeader
import com.kafecito.app.ui.components.ProductoImagen
import com.kafecito.app.ui.theme.*
import com.kafecito.app.util.comoDinero
import kotlinx.coroutines.launch

/**
 * Muestra los productos favoritos del usuario y permite
 * consultar sus detalles o eliminarlos de la lista.
 */
@Composable
fun FavoritosScreen(onAbrirDetalle: (Int) -> Unit) {
    val favoritos = remember { mutableStateListOf<Producto>() }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Obtiene los IDs de favoritos y busca sus productos en el catálogo.
LaunchedEffect(Unit) {
        try {
            val ids = FavoritosRepository.listar()
            favoritos.clear()
            favoritos.addAll(ids.mapNotNull { KafecitoRepository.obtenerProducto(it) })
        } catch (e: Exception) {
            error = "No se pudieron cargar tus favoritos"
        }
        cargando = false
    }

    Column(modifier = Modifier.fillMaxSize().background(KafeBlack)) {
        KafeHeader { Text(text = "FAVORITOS", style = MaterialTheme.typography.titleMedium) }
        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 20.dp))
        }
        when {
            cargando -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = KafeWhite)
            }

            favoritos.isEmpty() -> Text(
                text = "Aún no tienes favoritos",
                color = KafeGray,
                modifier = Modifier.padding(20.dp)
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(favoritos, key = { it.id }) { producto ->
                    FavoritoItem(
                        producto = producto,
                        onClick = { onAbrirDetalle(producto.id) },
                        onQuitar = {
                            scope.launch {
                                try {
                                    FavoritosRepository.quitar(producto.id)
                                    favoritos.removeAll { it.id == producto.id }
                                } catch (e: Exception) {
                                    error = "No se pudo quitar el favorito"
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoritoItem(
    producto: Producto,
    onClick: () -> Unit,
    onQuitar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KafeCard)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProductoImagen(
            imagen = producto.imagen,
            descripcion = producto.nombre,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = producto.nombre,
                color = KafeWhite,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = producto.precio.comoDinero(), color = KafeGray)
        }
        IconButton(onClick = onQuitar) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Quitar ${producto.nombre} de favoritos",
                tint = KafeWhite
            )
        }
    }
}
