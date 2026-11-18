package com.kafecito.app.util

import android.util.Patterns

/**
 * Contiene las validaciones de formularios utilizadas en Login, Registro y administración de productos.
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

    /**
     * Valida el formulario de productos del administrador: nombre no vacío,
     * precio mayor que 0 (máximo 1000) y stock mayor o igual a 0.
     *
     * Devuelve un mapa campo -> mensaje con los errores (vacío).
     */
    fun validarProducto(nombre: String, precio: String, stock: String): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        if (nombre.isBlank()) errores["nombre"] = "Escribe el nombre del producto"

        val p = precio.trim().replace(',', '.').toDoubleOrNull()
        when {
            p == null -> errores["precio"] = "Escribe un precio válido"
            p <= 0.0 -> errores["precio"] = "El precio debe ser mayor que 0"
            p > 1000.0 -> errores["precio"] = "El precio no puede superar \$1000"
        }

        val s = stock.trim().toIntOrNull()
        when {
            s == null -> errores["stock"] = "Escribe el stock (0 si no hay existencias)"
            s < 0 -> errores["stock"] = "El stock no puede ser negativo"
        }

        return errores
    }
}