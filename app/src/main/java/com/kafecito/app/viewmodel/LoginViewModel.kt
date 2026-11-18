package com.kafecito.app.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafecito.app.data.repository.AuthRepository
import com.kafecito.app.util.Validaciones
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    // Estados del formulario, modificables solo desde el ViewModel.
    var correo by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var errorMensaje by mutableStateOf<String?>(null)
        private set
    // Mensaje informativo (por ejemplo, confirmación de recuperación de contraseña).
    var infoMensaje by mutableStateOf<String?>(null)
        private set
    // Indica si hay una operación de autenticación en curso.
    var cargando by mutableStateOf(false)
        private set

    // Actualizan los valores ingresados en el formulario.
    fun onCorreoChange(value: String) { correo = value }
    fun onPasswordChange(value: String) { password = value }

    /**
     * Valida los datos ingresados e inicia sesión con Firebase.
     * Ejecuta [onExito] únicamente si las credenciales son correctas.
     */
    fun intentarLogin(onExito: () -> Unit) {
        if (cargando) return
        infoMensaje = null
        // Verifica que los campos obligatorios no estén vacíos.
        if (correo.isBlank() || password.isBlank()) {
            errorMensaje = "Correo y contraseña son obligatorios"
            return
        }
        // Comprueba el formato del correo antes de iniciar sesión.
        if (!Validaciones.esCorreoValido(correo.trim())) {
            errorMensaje = "El correo no tiene un formato válido"
            return
        }
        errorMensaje = null
        cargando = true
        viewModelScope.launch {
            val error = AuthRepository.login(correo.trim(), password)
            cargando = false
            if (error == null) onExito() else errorMensaje = error
        }
    }

    /** Inicia sesión con Google utilizando el contexto de una Activity. */
    fun loginConGoogle(context: Context, onExito: () -> Unit) {
        if (cargando) return
        infoMensaje = null
        errorMensaje = null
        cargando = true
        viewModelScope.launch {
            val error = AuthRepository.iniciarConGoogle(context)
            cargando = false
            if (error == null) onExito() else errorMensaje = error
        }
    }

    /** Solicita a Firebase enviar un enlace para restablecer la contraseña. */
    fun recuperarPassword() {
        if (cargando) return
        infoMensaje = null
        if (correo.isBlank() || !Validaciones.esCorreoValido(correo.trim())) {
            errorMensaje = "Escribe tu correo para recuperar la contraseña"
            return
        }
        errorMensaje = null
        cargando = true
        viewModelScope.launch {
            val error = AuthRepository.recuperarPassword(correo.trim())
            cargando = false
            if (error == null) {
                infoMensaje = "Si el correo está registrado, te enviamos un enlace para restablecer la contraseña"
            } else {
                errorMensaje = error
            }
        }
    }
}
