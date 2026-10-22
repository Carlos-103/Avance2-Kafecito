package com.kafecito.app.data.model

/** Documento usuarios/{uid}/direcciones/{id}. */
data class Direccion(
    val id: String = "",
    val alias: String = "",
    val calle: String = "",
    val ciudad: String = "",
    val referencia: String = ""
)
