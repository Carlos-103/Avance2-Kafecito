package com.kafecito.app.data.repository

import com.kafecito.app.R
import com.kafecito.app.data.model.Producto
import kotlinx.coroutines.tasks.await

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
    private var cache: List<Producto> = KafecitoRepository.obtenerProductos()

    fun obtenerProductos(categoria: String? = null): List<Producto> =
        if (categoria == null) cache else cache.filter { it.categoria == categoria }

    fun obtenerProducto(id: Int): Producto? = cache.find { it.id == id }

    /** Nombre guardado en Firestore -> imagen de res/drawable. Sin nombre válido no hay imagen. */
    val imagenes: Map<String, Int> = mapOf(
        "cappucino" to R.drawable.cappucino,
        "cafe_americano" to R.drawable.cafe_americano,
        "pastel_chocolate" to R.drawable.pastel_chocolate,
        "sprite" to R.drawable.sprite,
        "desayuno_tipico" to R.drawable.desayuno_tipico
    )

    // =====================================================================
    // TODO(CATALOGO-FS) - Carlos David
    // Carga el catálogo desde Firestore y lo guarda en `cache`.
    //   1. val snap = FirestorePaths.productos().get().await()
    //   2. Convertir cada documento a Producto: id, nombre, descripcion, precio, stock,
    //      categoria e imagen (buscar el texto en `imagenes`; si no existe, null).
    //   3. Si la lectura falla o no hay documentos, dejar el catálogo local (no vaciar la app).
    //   4. Llamarla al abrir Home: LaunchedEffect(Unit) { ProductoRepository.cargar() }
    //      y refrescar la lista cuando termine (ver el TODO en HomeScreen).
    // Los 5 productos iniciales los carga el dueño del repositorio en la consola de Firebase.
    // =====================================================================
    suspend fun cargar() {
        // Pendiente.
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
        val nombreImagen = imagenes.entries.firstOrNull { it.value == producto.imagen }?.key
        val datos = hashMapOf<String, Any?>(
            "id" to producto.id,
            "nombre" to producto.nombre,
            "descripcion" to producto.descripcion,
            "precio" to producto.precio,
            "stock" to producto.stock,
            "categoria" to producto.categoria,
            "imagen" to nombreImagen
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
