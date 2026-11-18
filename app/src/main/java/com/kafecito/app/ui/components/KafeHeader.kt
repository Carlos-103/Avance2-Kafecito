package com.kafecito.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kafecito.app.ui.theme.KafeWhite

/**
 * Encabezado con fondo blanco que organiza el contenido
 * de la parte superior de la pantalla.
 */
@Composable
fun KafeHeader(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KafeWhite)
            .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 20.dp),
        content = content
    )
}