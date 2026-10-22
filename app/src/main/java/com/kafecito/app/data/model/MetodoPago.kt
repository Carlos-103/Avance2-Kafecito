package com.kafecito.app.data.model

/**
 * Documento usuarios/{uid}/metodosPago/{id}.
 * Por seguridad solo se guardan la marca y los ultimos 4 digitos, nunca el numero completo.
 */
data class MetodoPago(
    val id: String = "",
    val marca: String = "",
    val ultimos4: String = ""
) {
    val etiqueta: String get() = "$marca \u2022\u2022\u2022\u2022 $ultimos4"
}
