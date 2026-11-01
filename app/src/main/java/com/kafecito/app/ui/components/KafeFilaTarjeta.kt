package com.kafecito.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kafecito.app.ui.theme.KafeCard
import com.kafecito.app.ui.theme.KafeGray
import com.kafecito.app.ui.theme.KafeWhite

/**
 * Fila reutilizable que muestra un título y un subtítulo a la izquierda
 * y permite agregar una acción opcional a la derecha, como un botón de eliminar.
 */
@Composable
fun KafeFilaTarjeta(
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KafeCard)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, color = KafeWhite, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitulo, color = KafeGray, style = MaterialTheme.typography.bodyMedium)
        }
        accion?.invoke()
    }
}
