package com.kafecito.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
// Define una paleta base fija, independiente del modo oscuro del teléfono.
private val KafeColors = lightColorScheme(
    primary = KafeBlack,
    onPrimary = KafeWhite,
    secondary = KafeGray,
    background = KafeWhite,
    surface = KafeWhite,
    onBackground = KafeBlack,
    onSurface = KafeBlack,
    error = ErrorRed
)
/**
 * Aplica los colores y la tipografía
 * a los componentes que se encuentran dentro del tema.
 */
@Composable
fun KafecitoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KafeColors,
        typography = Typography,
        content = content
    )
}
