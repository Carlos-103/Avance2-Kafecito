package com.kafecito.app.data.model

/** Representa una dirección almacenada en usuarios/{uid}/direcciones/{id}. */
data class Direccion(
    val id: String = "",
    val alias: String = "",
    val calle: String = "",
    val ciudad: String = "",
    val referencia: String = ""
)
