package com.kafecito.app.data.repository

import com.kafecito.app.data.model.Direccion

/**
 * Direcciones del usuario: usuarios/{uid}/direcciones/{id}.
 * Mientras no se implemente cada funcion devuelve un valor seguro (lista vacia)
 * para que la app no se caiga si otra pantalla ya la llama.
 */
object DireccionRepository {

    // TODO(DIRECCIONES): patron "leer lista".
    suspend fun listar(): List<Direccion> {
        return emptyList()
    }

    // TODO(DIRECCIONES): patron "agregar" (el id lo genera Firestore con document()).
    suspend fun agregar(direccion: Direccion) {
        // Pendiente.
    }

    // TODO(DIRECCIONES): patron "borrar".
    suspend fun borrar(id: String) {
        // Pendiente.
    }
}
