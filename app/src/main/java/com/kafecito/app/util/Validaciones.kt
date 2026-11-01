package com.kafecito.app.util

import android.util.Patterns

/**
 * Contiene las validaciones de formularios utilizadas en Login y Registro.
 * Cumple con el requisito 4 del Avance 1.
 */
object Validaciones {

    const val LARGO_MINIMO_PASSWORD = 6
    // Comprueba que el correo tenga un formato válido.
    fun esCorreoValido(correo: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(correo).matches()
    // Comprueba que la contraseña tenga al menos la longitud mínima requerida.
    fun esPasswordValida(password: String): Boolean =
        password.length >= LARGO_MINIMO_PASSWORD
}
