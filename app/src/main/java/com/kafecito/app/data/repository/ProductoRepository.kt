package com.kafecito.app.data.repository

import com.kafecito.app.R
import com.kafecito.app.data.model.Producto
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CancellationException

/**
 * Gestiona el catálogo de productos de Kafecito mediante Firestore.
 * Mantiene una copia en memoria para facilitar las consultas desde las pantallas.
 *
 * También permite crear, editar y eliminar productos.
 */
object ProductoRepository {

    val categorias: List<String> = KafecitoRepository.categorias

    // Copia en memoria del catálogo para consultar productos sin realizar otra lectura de Firestore.
    private var cache: List<Producto> = emptyList()

    fun obtenerProductos(categoria: String? = null): List<Producto> =
        if (categoria == null) cache else cache.filter { it.categoria == categoria }

    fun obtenerProducto(id: Int): Producto? = cache.find { it.id == id }
    /**
     * Carga los productos desde Firestore y actualiza la copia en memoria.
     * Si ocurre un error de lectura, conserva el catálogo disponible.
     */
    suspend fun cargar() {
        try {
            val snap = FirestorePaths.productos().get().await()
            val productos = snap.documents.mapNotNull { it.aProducto() }
            cache = productos.sortedBy { it.id }
        } catch (e: CancellationException) {
            throw e // Permite que la cancelación de la corrutina se propague correctamente.
        } catch (e: Exception) {
            // Si falla la lectura, se conserva el catálogo anterior.
        }
    }
    /** Convierte un documento de Firestore en un producto; devuelve null si faltan datos obligatorios. */
    private fun DocumentSnapshot.aProducto(): Producto? {
        val id = (get("id") as? Number)?.toInt() ?: id.toIntOrNull() ?: return null
        val nombre = getString("nombre") ?: return null
        val precio = (get("precio") as? Number)?.toDouble() ?: return null

        return Producto(
            id = id,
            nombre = nombre,
            descripcion = getString("descripcion") ?: "",
            precio = precio,
            stock = (get("stock") as? Number)?.toInt() ?: 0,
            categoria = getString("categoria") ?: "",
            imagenUrl = getString("imagenUrl")?.takeIf { it.isNotBlank() }
        )
    }

    /** Obtiene el siguiente ID disponible a partir del mayor ID del catálogo en memoria. */
    fun siguienteId(): Int = (cache.maxOfOrNull { it.id } ?: 0) + 1

    /** Guarda o actualiza un producto en Firestore y sincroniza la copia en memoria. */
    suspend fun guardar(producto: Producto) {
        val datos = hashMapOf<String, Any?>(
            "id" to producto.id,
            "nombre" to producto.nombre,
            "descripcion" to producto.descripcion,
            "precio" to producto.precio,
            "stock" to producto.stock,
            "categoria" to producto.categoria,
            "imagenUrl" to producto.imagenUrl
        )
        FirestorePaths.producto(producto.id).set(datos).await()
        cache = (cache.filter { it.id != producto.id } + producto).sortedBy { it.id }
    }

    /** Elimina un producto de Firestore y de la copia en memoria. */
    suspend fun borrar(id: Int) {
        FirestorePaths.producto(id).delete().await()
        cache = cache.filter { it.id != id }
    }
}
