package com.kafecito.app.data.model

/**
 * Representa el documento usuarios/{uid} en Firestore.
 * No almacena contraseñas, ya que Firebase Authentication se encarga de ellas.
 *
 * Los valores por defecto permiten que Firestore cree instancias del modelo
 * al leer los documentos.
 */
data class PerfilUsuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    // Por defecto, el usuario tiene el rol CLIENTE. Para asignar ADMIN, se cambia desde la consola de Firebase.
    val rol: String = "CLIENTE"
)
