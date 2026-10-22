package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kafecito.app.ui.components.KafeDarkField
import com.kafecito.app.ui.components.KafeGoogleButton
import com.kafecito.app.ui.theme.KafeBlack
import com.kafecito.app.ui.theme.KafeGray
import com.kafecito.app.ui.theme.KafeWhite
import com.kafecito.app.viewmodel.LoginViewModel

/*
 * Pantalla de inicio de sesión.
 * Permite entrar con correo y contraseña o con Google (Firebase Authentication).
 */
@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    onAtras: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    // Para Google hace falta la Activity (muestra el selector de cuentas).
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KafeBlack)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Permite regresar a la pantalla anterior.
        IconButton(onClick = onAtras) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = KafeWhite)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "KAFECITO", color = KafeGray, style = MaterialTheme.typography.bodyMedium)
        Text(text = "Bienvenido de nuevo", color = KafeWhite, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(32.dp))
        // Los campos se conectan al ViewModel para mantener los datos ingresados.
        KafeDarkField(
            label = "Correo electrónico",
            value = viewModel.correo,
            onChange = viewModel::onCorreoChange,
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(16.dp))
        KafeDarkField(
            label = "Contraseña",
            value = viewModel.password,
            onChange = viewModel::onPasswordChange,
            keyboardType = KeyboardType.Password,
            esPassword = true
        )

        // Recuperar contraseña: envía el correo de Firebase al correo escrito arriba.
        TextButton(
            onClick = viewModel::recuperarPassword,
            enabled = !viewModel.cargando,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("¿Olvidaste tu contraseña?", color = KafeGray, style = MaterialTheme.typography.bodySmall)
        }

        // El mensaje de error aparece únicamente cuando existe uno.
        viewModel.errorMensaje?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        // Mensaje informativo (por ejemplo, correo de recuperación enviado).
        viewModel.infoMensaje?.let {
            Text(text = it, color = KafeWhite)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            // Si las credenciales son válidas, se notifica el inicio de sesión exitoso.
            onClick = { viewModel.intentarLogin(onLoginExitoso) },
            enabled = !viewModel.cargando,
            colors = ButtonDefaults.buttonColors(containerColor = KafeWhite, contentColor = KafeBlack),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text(if (viewModel.cargando) "ESPERA..." else "INICIAR SESIÓN") }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "o",
            color = KafeGray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        KafeGoogleButton(
            texto = "Iniciar sesión con Google",
            enabled = !viewModel.cargando
        ) { viewModel.loginConGoogle(context, onLoginExitoso) }
    }
}
