package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kafecito.app.data.repository.AuthRepository
import com.kafecito.app.ui.components.KafeDarkField
import com.kafecito.app.ui.components.KafeGoogleButton
import com.kafecito.app.ui.theme.KafeBlack
import com.kafecito.app.ui.theme.KafeGray
import com.kafecito.app.ui.theme.KafeWhite
import com.kafecito.app.util.Validaciones
import kotlinx.coroutines.launch

/**
 * Pantalla para crear una cuenta mediante correo electrónico
 * y contraseña o utilizando una cuenta de Google.
 *
 * Valida los datos ingresados y registra al usuario en Firebase.
 */
@Composable
fun RegisterScreen(
    onCuentaCreada: () -> Unit,
    onAtras: () -> Unit
) {
    // Estados que almacenan los datos ingresados y los errores del formulario.
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var aceptaTerminos by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    // Indica si hay una operación de registro en curso.
    var cargando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
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

        Text(text = "KAFECITO", color = KafeGray, style = MaterialTheme.typography.bodyMedium)
        Text(text = "Crear cuenta", color = KafeWhite, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(24.dp))

        KafeDarkField(label = "Nombre completo", value = nombre, onChange = { nombre = it })
        Spacer(modifier = Modifier.height(12.dp))
        KafeDarkField(label = "Correo", value = correo, onChange = { correo = it }, keyboardType = KeyboardType.Email)
        Spacer(modifier = Modifier.height(12.dp))
        KafeDarkField(label = "Contraseña", value = password, onChange = { password = it }, esPassword = true)
        Spacer(modifier = Modifier.height(12.dp))
        KafeDarkField(label = "Confirmar contraseña", value = confirmar, onChange = { confirmar = it }, esPassword = true)


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Checkbox(
                checked = aceptaTerminos,
                onCheckedChange = { aceptaTerminos = it },
                colors = CheckboxDefaults.colors(checkedColor = KafeWhite, uncheckedColor = KafeGray, checkmarkColor = KafeBlack)
            )
            Text("Acepto términos y condiciones", color = KafeGray)
        }

        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (cargando) return@Button
                // Primero se validan los datos antes de intentar registrar al usuario.
                val errorValidacion = validarRegistro(nombre, correo, password, confirmar, aceptaTerminos)
                if (errorValidacion != null) {
                    error = errorValidacion
                } else {
                    error = null
                    cargando = true
                    // Ejecuta el registro en una corrutina porque la operación de Firebase es asíncrona.
                    scope.launch {
                        val resultado = AuthRepository.registrar(nombre.trim(), correo.trim(), password)
                        cargando = false
                        if (resultado == null) onCuentaCreada() else error = resultado
                    }
                }
            },
            enabled = !cargando,
            colors = ButtonDefaults.buttonColors(containerColor = KafeWhite, contentColor = KafeBlack),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text(if (cargando) "ESPERA..." else "CREAR CUENTA") }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "o",
            color = KafeGray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Permite registrarse o iniciar sesión con Google; Firebase gestiona ambos casos.
        KafeGoogleButton(texto = "Registrarse con Google", enabled = !cargando) {
            error = null
            cargando = true
            scope.launch {
                val resultado = AuthRepository.iniciarConGoogle(context)
                cargando = false
                if (resultado == null) onCuentaCreada() else error = resultado
            }
        }
    }
}

/**
 * Comprueba los datos del formulario y devuelve el primer error encontrado.
 * Devuelve null si todas las validaciones son correctas.
 */
private fun validarRegistro(
    nombre: String,
    correo: String,
    password: String,
    confirmar: String,
    aceptaTerminos: Boolean
): String? {
    // Comprueba que todos los campos obligatorios estén completos.
    if (nombre.isBlank() || correo.isBlank() || password.isBlank() || confirmar.isBlank()) {
        return "Todos los campos son obligatorios"
    }
    // Verifica que el correo electrónico tenga un formato valido.
    if (!Validaciones.esCorreoValido(correo.trim())) return "El correo no tiene un formato válido"

    // Comprueba que la contraseña cumpla con la longitud mínima
    if (!Validaciones.esPasswordValida(password)) {
        return "La contraseña debe tener al menos ${Validaciones.LARGO_MINIMO_PASSWORD} caracteres"
    }
    // Verifica que ambas contraseñas coincidan.
    if (password != confirmar) return "Las contraseñas no coinciden"
    // Comprueba que el usuario haya aceptado los términos y condiciones.
    if (!aceptaTerminos) return "Debes aceptar los términos y condiciones"
    return null
}
