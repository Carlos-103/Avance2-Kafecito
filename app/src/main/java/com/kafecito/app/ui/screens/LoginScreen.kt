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

/**
 * Pantalla de inicio de sesión mediante correo electrónico
 * y contraseña o una cuenta de Google.
 */
@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    onAtras: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    // Obtiene el contexto necesario para iniciar el selector de cuentas de Google.
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KafeBlack)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        IconButton(onClick = onAtras) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = KafeWhite)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "KAFECITO", color = KafeGray, style = MaterialTheme.typography.bodyMedium)
        Text(text = "Bienvenido de nuevo", color = KafeWhite, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(32.dp))
        // Los campos utilizan el ViewModel para guardar los datos ingresados.
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

        // Solicita a Firebase enviar un correo para recuperar la contraseña.
        TextButton(
            onClick = viewModel::recuperarPassword,
            enabled = !viewModel.cargando,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("¿Olvidaste tu contraseña?", color = KafeGray, style = MaterialTheme.typography.bodySmall)
        }


        viewModel.errorMensaje?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        viewModel.infoMensaje?.let {
            Text(text = it, color = KafeWhite)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            // Intenta iniciar sesión y notifica al componente padre si tiene éxito.
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
