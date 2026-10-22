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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kafecito.app.data.model.Producto
import com.kafecito.app.data.repository.KafecitoRepository
import com.kafecito.app.ui.components.KafeHeader
import com.kafecito.app.ui.components.ProductoImagen
import com.kafecito.app.ui.theme.*
import com.kafecito.app.util.comoDinero

/** Pantalla de favoritos (pestaña de la barra inferior). Hoy usa datos de ejemplo. */
@Composable
fun FavoritosScreen(onAbrirDetalle: (Int) -> Unit) {
    // TODO(FAVORITOS): reemplazar por los productos cuyos ids devuelve FavoritosRepository.listar()
    // (buscar cada uno con KafecitoRepository.obtenerProducto(id)).
    val favoritos = remember {
        mutableStateListOf<Producto>().apply { addAll(KafecitoRepository.obtenerProductos().take(2)) }
    }

    Column(modifier = Modifier.fillMaxSize().background(KafeBlack)) {
        KafeHeader { Text(text = "FAVORITOS", style = MaterialTheme.typography.titleMedium) }

        if (favoritos.isEmpty()) {
            Text(text = "Aún no tienes favoritos", color = KafeGray, modifier = Modifier.padding(20.dp))
        } else {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(favoritos, key = { it.id }) { producto ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(KafeCard)
                            .clickable { onAbrirDetalle(producto.id) }
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
                            Text(text = producto.nombre, color = KafeWhite, style = MaterialTheme.typography.bodyLarge)
                            Text(text = producto.precio.comoDinero(), color = KafeGray)
                        }
                        // TODO(FAVORITOS): ademas de quitarlo de la lista, llamar a FavoritosRepository.quitar(producto.id)
                        IconButton(onClick = { favoritos.remove(producto) }) {
                            Icon(Icons.Filled.Favorite, contentDescription = "Quitar de favoritos", tint = KafeWhite)
                        }
                    }
                }
            }
        }
    }
}
