package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kafecito.app.ui.theme.KafeBlack
import com.kafecito.app.ui.theme.KafeWhite

/**
 * Pantalla de bienvenida de Kafecito.
 * Permite iniciar sesión o crear una cuenta.
 */
@Composable
fun WelcomeScreen(
    onIniciarSesion: () -> Unit,
    onCrearCuenta: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KafeBlack)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Contenedor circular para el icono o logo de Kafecito.
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(KafeWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Filled.Coffee,
                    contentDescription = null,
                    tint = KafeBlack,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "KAFECITO",
                color = KafeWhite,
                style = MaterialTheme.typography.titleLarge
            )
        }
        // Coloca los botones en la parte inferior de la pantalla.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Navega a la pantalla de inicio de sesión.
            Button(
                onClick = onIniciarSesion,
                colors = ButtonDefaults.buttonColors(containerColor = KafeWhite, contentColor = KafeBlack),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("INICIAR SESIÓN") }
// Navega al formulario de creación de cuenta.
            OutlinedButton(
                onClick = onCrearCuenta,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KafeWhite),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("CREAR CUENTA") }
        }
    }
}
