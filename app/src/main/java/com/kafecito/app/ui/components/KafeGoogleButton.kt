package com.kafecito.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kafecito.app.ui.theme.KafeBorderDark
import com.kafecito.app.ui.theme.KafeGray
import com.kafecito.app.ui.theme.KafeWhite

/** Boton "Iniciar sesion con Google" para pantallas con fondo oscuro (Login y Registro). */
@Composable
fun KafeGoogleButton(
    texto: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(1.dp, KafeBorderDark),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = KafeWhite,
            disabledContentColor = KafeGray
        ),
        modifier = Modifier.fillMaxWidth().height(48.dp)
    ) { Text(texto) }
}
