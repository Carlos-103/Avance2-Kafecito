package com.kafecito.app.data.repository

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kafecito.app.data.model.EstadoPedido
import com.kafecito.app.data.model.Pedido
import kotlinx.coroutines.tasks.await

/**
 * Gestiona los pedidos de cada usuario en Firestore.
 * Permite registrar pedidos, consultar el historial y calcular puntos acumulados.
 */
object PedidoRepository {

    /** Calcula los puntos obtenidos: 1 punto por cada dólar del total,
     * sin redondear hacia arriba. */
    fun puntosDe(total: Double): Int = total.toInt()

    /**
     * Guarda un pedido en Firestore y devuelve el ID del documento creado.
     * Si ocurre un error, lanza una excepción para evitar vaciar el carrito
     * sin haber guardado correctamente el pedido.
     */
    suspend fun crear(pedido: Pedido): String {
        require(pedido.items.isNotEmpty()) { "El pedido no tiene productos" }
        val uid = FirestorePaths.uidActual()
        val ref = FirestorePaths.pedidos(uid).document()  // Firestore genera automáticamente el ID del pedido.
        ref.set(pedido.copy(id = ref.id, uid = uid)).await()
        return ref.id
    }

    /** Obtiene el historial de pedidos del usuario, ordenado del más reciente al más antiguo. */
    suspend fun listar(): List<Pedido> {
        val uid = FirestorePaths.uidActual()
        val snap = FirestorePaths.pedidos(uid)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .await()
        // Se copia el id del documento al objeto.
        return snap.documents.mapNotNull { doc ->
            doc.toObject(Pedido::class.java)?.copy(id = doc.id)
        }
    }

    /** Obtiene la cantidad total de pedidos del usuario para las estadísticas del perfil. */
    suspend fun contar(): Int {
        val uid = FirestorePaths.uidActual()
        val resultado = FirestorePaths.pedidos(uid).count().get(AggregateSource.SERVER).await()
        return resultado.count.toInt()
    }

    /** Calcula los puntos acumulados sumando los puntos obtenidos en cada pedido. */
    suspend fun puntosTotales(): Int = listar().sumOf { puntosDe(it.total) }

    /**
     * Crea un pedido y descuenta las unidades correspondientes del inventario.
     * Ambas operaciones se realizan en una transacción para evitar inconsistencias
     * si no hay stock suficiente o ocurre algún error.
     */
    suspend fun crearConStock(pedido: Pedido): String {
        require(pedido.items.isNotEmpty()) { "El pedido no tiene productos" }
        val uid = FirestorePaths.uidActual()
        val refPedido = FirestorePaths.pedidos(uid).document()
        FirebaseFirestore.getInstance().runTransaction { tx ->
            // Primero se consultan todos los productos, antes de realizar cualquier escritura.
            val nuevosStocks = pedido.items.map { item ->
                val ref = FirestorePaths.producto(item.productoId)
                val stock = tx.get(ref).getLong("stock")?.toInt() ?: 0
                if (stock < item.cantidad) {
                    throw IllegalStateException("Sin stock suficiente de ${item.nombre}")
                }
                ref to (stock - item.cantidad)
            }
            // Después se actualiza el inventario y se guarda el pedido.
            nuevosStocks.forEach { (ref, nuevo) -> tx.update(ref, "stock", nuevo) }
            tx.set(refPedido, pedido.copy(id = refPedido.id, uid = uid, estado = EstadoPedido.PENDIENTE))
            null
        }.await()
        return refPedido.id
    }

    /** Adquiere los pedidos de todos los usuarios, ordenados del más reciente al más antiguo. */
    suspend fun listarTodos(): List<Pedido> {
        val snap = FirestorePaths.todosLosPedidos()
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            // Obtiene el UID del propietario desde la ruta del documento en Firestore.
            doc.toObject(Pedido::class.java)
                ?.copy(id = doc.id, uid = doc.reference.parent.parent?.id ?: "")
        }
    }

    /** Actualiza el estado de un pedido. Esta operación está destinada al administrador. */
    suspend fun cambiarEstado(uid: String, pedidoId: String, estado: String) {
        FirestorePaths.pedidos(uid).document(pedidoId).update("estado", estado).await()
    }
}
