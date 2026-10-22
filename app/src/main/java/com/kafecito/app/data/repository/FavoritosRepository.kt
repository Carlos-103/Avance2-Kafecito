package com.kafecito.app.data.repository

/**
 * Favoritos del usuario: usuarios/{uid}/favoritos/{productoId}.
 * El id del documento es el id del producto, asi no se puede repetir.
 * Patrones en GUIA_FIRESTORE.md.
 *
 * Mientras no se implemente cada funcion devuelve un valor seguro (lista vacia o 0)
 * para que la app no se caiga si otra pantalla ya la llama.
 */
object FavoritosRepository {

    // TODO(FAVORITOS): patron "leer lista". Devuelve los ids de producto favoritos.
    suspend fun listar(): List<Int> {
        return emptyList()
    }

    // TODO(FAVORITOS): patron "agregar" (con document(productoId.toString()).set(...)).
    suspend fun agregar(productoId: Int) {
        // Pendiente.
    }

    // TODO(FAVORITOS): patron "borrar".
    suspend fun quitar(productoId: Int) {
        // Pendiente.
    }

    // TODO(FAVORITOS): patron "contar" (se usa en las estadisticas del Perfil).
    suspend fun contar(): Int {
        return 0
    }
}
