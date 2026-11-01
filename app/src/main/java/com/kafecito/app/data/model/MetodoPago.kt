package com.kafecito.app.data.model

/**
 * Representa un metodo de pago almacenado en usuarios/{uid}/metodosPago/{id}.
 * Por seguridad, solo guarda la marca de la tarjeta y sus últimos cuatro dígitos.
 * Nunca almacena el número completo.
 */
data class MetodoPago(
    val id: String = "",
    val marca: String = "",
    val ultimos4: String = ""
) {
    val etiqueta: String get() = "$marca \u2022\u2022\u2022\u2022 $ultimos4"
}
