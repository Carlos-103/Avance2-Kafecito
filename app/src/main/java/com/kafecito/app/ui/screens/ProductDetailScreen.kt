package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kafecito.app.data.model.Producto
import com.kafecito.app.data.repository.FavoritosRepository
import com.kafecito.app.ui.components.ProductoImagen
import com.kafecito.app.ui.theme.*
import kotlinx.coroutines.launch
@Composable
fun ProductDetailScreen(
    producto: Producto?,
    onAtras: () -> Unit,
    onAgregar: (Producto, Int) -> Unit
) {
    val scope = rememberCoroutineScope()

    // Estado de favorito. Se reinicia si cambia el producto.
    var esFavorito by remember(producto?.id) { mutableStateOf(false) }
    var favoritoCargado by remember(producto?.id) { mutableStateOf(false) }

    // Consulta en Firestore si el producto ya es favorito al abrir la pantalla
    LaunchedEffect(producto?.id) {
        if (producto != null) {
            esFavorito = FavoritosRepository.esFavorito(producto.id)
            favoritoCargado = true
        }
    }

    // Alterna el favorito con actualización optimista: cambia la UI al instante
    // y revierte si Firestore falla.
    fun alternarFavorito(p: Producto) {
        val nuevoEstado = !esFavorito
        esFavorito = nuevoEstado
        scope.launch {
            try {
                if (nuevoEstado) FavoritosRepository.agregar(p.id)
                else FavoritosRepository.quitar(p.id)
            } catch (e: Exception) {
                esFavorito = !nuevoEstado
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KafeBlack)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAtras) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = KafeWhite)
            }
            if (producto != null) {
                IconButton(
                    onClick = { alternarFavorito(producto) },
                    enabled = favoritoCargado
                ) {
                    Icon(
                        imageVector = if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (esFavorito) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = KafeWhite
                    )
                }
            }
        }

        if (producto == null) {
            Text("Producto no encontrado", color = KafeWhite)
            return@Column
        }

        // Cantidad seleccionada (mínimo 1, máximo el stock disponible)
        var cantidad by remember { mutableStateOf(1) }

        ProductoImagen(
            imagen = producto.imagen,
            descripcion = producto.nombre,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = producto.nombre, color = KafeWhite, style = MaterialTheme.typography.titleMedium)
            Text(text = "$${"%.2f".format(producto.precio)}", color = KafeWhite, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = producto.descripcion, color = KafeGray, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(20.dp))

        // Selector de cantidad: [ - ]  N  [ + ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón "-" : resta 1 pero nunca baja de 1
            FilledIconButton(
                onClick = { if (cantidad > 1) cantidad-- },
                enabled = cantidad > 1,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = KafeWhite,
                    contentColor = KafeBlack,
                    disabledContainerColor = KafeGray,
                    disabledContentColor = KafeBlack
                )
            ) {
                Text("−", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "$cantidad",
                color = KafeWhite,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 28.dp)
            )

            // Botón "+" : suma 1 pero nunca supera el stock
            FilledIconButton(
                onClick = { if (cantidad < producto.stock) cantidad++ },
                enabled = cantidad < producto.stock,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = KafeWhite,
                    contentColor = KafeBlack,
                    disabledContainerColor = KafeGray,
                    disabledContentColor = KafeBlack
                )
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // El botón muestra el total: precio * cantidad (ej. "Agregar pedido $3.50")
        Button(
            onClick = {
                onAgregar(producto, cantidad)
                onAtras()
            },
            colors = ButtonDefaults.buttonColors(containerColor = KafeWhite, contentColor = KafeBlack),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Agregar pedido $${"%.2f".format(producto.precio * cantidad)}")
        }
    }
}
