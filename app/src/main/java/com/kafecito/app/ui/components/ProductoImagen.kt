package com.kafecito.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.kafecito.app.ui.theme.KafeImagePlaceholder
/**
 * Muestra la imagen de un producto o un espacio temporal
 * cuando todavía no se dispone de una imagen.
 */
@Composable
fun ProductoImagen(imagen: Int?, descripcion: String, modifier: Modifier = Modifier) {
    if (imagen != null) {
        Image(
            painter = painterResource(imagen),
            contentDescription = descripcion,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        // Muestra un fondo gris cuando no hay imagen disponible.
        Box(modifier = modifier.background(KafeImagePlaceholder))
    }
}