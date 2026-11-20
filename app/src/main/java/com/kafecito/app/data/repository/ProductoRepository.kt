package com.kafecito.app.data.repository

import com.kafecito.app.R
import com.kafecito.app.data.model.Producto
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CancellationException

/**
 * Catálogo de productos que usa toda la app (Home, Detalle, Favoritos, Repetir pedido).
 *
 * Por ahora devuelve el catálogo local de KafecitoRepository, así la app sigue funcionando
 * mientras se migra a Firestore. Las pantallas ya llaman a este objeto, así que al
 * implementar los TODO de abajo no hay que tocar las demás pantallas.
 *
 * Formato de cada documento en Firestore: productos/{id}
 *   id (número), nombre, descripcion, precio (número), stock (número), categoria,
 *   imagen (texto con el nombre del recurso, por ejemplo "cappucino"; opcional).
 */
object ProductoRepository {

    val categorias: List<String> = KafecitoRepository.categorias

    // Copia en memoria del catálogo. Así obtenerProducto(id) sigue siendo una función normal.
    private var cache: List<Producto> = emptyList()

    fun obtenerProductos(categoria: String? = null): List<Producto> =
        if (categoria == null) cache else cache.filter { it.categoria == categoria }

    fun obtenerProducto(id: Int): Producto? = cache.find { it.id == id }

    suspend fun cargar() {
        try {
            val snap = FirestorePaths.productos().get().await()
            val productos = snap.documents.mapNotNull { it.aProducto() }
            cache = productos.sortedBy { it.id }
        } catch (e: CancellationException) {
            throw e // no se debe tragar la cancelación de la corutina
        } catch (e: Exception) {
            // Si la lectura falla, se deja el catálogo local (no vaciar la app).
        }
    }
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

    // =====================================================================
    // Administración de productos (solo administrador; lo hacen cumplir las reglas).
    // Ya implementado por el dueño del repositorio. La pantalla de Ricardo usa
    // AdminProductosViewModel, que llama a estas funciones.
    // =====================================================================

    /** Id para un producto nuevo: el mayor id actual + 1. */
    fun siguienteId(): Int = (cache.maxOfOrNull { it.id } ?: 0) + 1

    /** Crea o edita un producto en Firestore y actualiza la copia en memoria. */
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

    /** Borra un producto de Firestore y de la copia en memoria. */
    suspend fun borrar(id: Int) {
        FirestorePaths.producto(id).delete().await()
        cache = cache.filter { it.id != id }
    }
}
