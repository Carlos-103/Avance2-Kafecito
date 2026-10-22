package com.kafecito.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


// Define los estilos de texto utilizados en la aplicación.
val Typography = Typography(
    // Estilos para títulos principales y secundarios.
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
    // Estilos para el texto del contenido.
    bodyLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp),
    // Estilo para etiquetas y textos de botones.
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp)
)
