package com.kafecito.app.data.model

// Usuario del Avance 1 (en memoria, con contraseña).
// TODO(AUTH-CORREO): al migrar a Firebase, borrar esta clase y usar PerfilUsuario.
data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val password: String
)

/**
 * Documento usuarios/{uid} en Firestore. Nunca se guarda la contraseña
 * (eso lo maneja Firebase Authentication).
 * Los valores por defecto son necesarios para que Firestore pueda leerlo.
 */
data class PerfilUsuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    // "CLIENTE" por defecto. Para ser admin se cambia a mano en la consola de Firebase.
    val rol: String = "CLIENTE"
)
