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
        val ref = FirestorePaths.pedidos(uid).document()   // Firestore genera el id
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

    // =====================================================================
    // TODO(STOCK) - Kevin
    // Crea el pedido Y descuenta el stock en una sola operacion (transaccion).
    // Esta funcion ya viene escrita. Lo que falta:
    //   1. En PagarViewModel, cambiar PedidoRepository.crear(pedido) por
    //      PedidoRepository.crearConStock(pedido).
    //   2. Mostrar un mensaje claro si no hay stock (el error trae el nombre del producto).
    //   3. En PedidosScreen, mostrar el estado del pedido (pedido.estado).
    // Probar con internet: una transaccion falla sin conexion (no queda en cola).
    // Nota: el mensaje de la excepcion puede venir envuelto; revisar en Logcat.
    // =====================================================================
    suspend fun crearConStock(pedido: Pedido): String {
        require(pedido.items.isNotEmpty()) { "El pedido no tiene productos" }
        val uid = FirestorePaths.uidActual()
        val refPedido = FirestorePaths.pedidos(uid).document()
        FirebaseFirestore.getInstance().runTransaction { tx ->
            // 1) Primero se leen todos los productos (en una transaccion las lecturas van antes de escribir).
            val nuevosStocks = pedido.items.map { item ->
                val ref = FirestorePaths.producto(item.productoId)
                val stock = tx.get(ref).getLong("stock")?.toInt() ?: 0
                if (stock < item.cantidad) {
                    throw IllegalStateException("Sin stock suficiente de ${item.nombre}")
                }
                ref to (stock - item.cantidad)
            }
            // 2) Despues se descuenta el stock y se guarda el pedido.
            nuevosStocks.forEach { (ref, nuevo) -> tx.update(ref, "stock", nuevo) }
            tx.set(refPedido, pedido.copy(id = refPedido.id, uid = uid, estado = EstadoPedido.PENDIENTE))
            null
        }.await()
        return refPedido.id
    }

    // =====================================================================
    // TODO(ADMIN-PEDIDOS) - Jose
    // Estas dos funciones ya vienen escritas; usalas desde AdminPedidosViewModel.
    // Requieren: reglas de Firestore publicadas y el indice de grupo de coleccion
    // por "fecha" (si falta, el error en Logcat trae un enlace para crearlo).
    // =====================================================================

    /** Pedidos de TODOS los usuarios, del mas reciente al mas antiguo (solo administrador). */
    suspend fun listarTodos(): List<Pedido> {
        val snap = FirestorePaths.todosLosPedidos()
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            // El uid del dueno es el id del documento "usuarios/{uid}" que contiene al pedido.
            doc.toObject(Pedido::class.java)
                ?.copy(id = doc.id, uid = doc.reference.parent.parent?.id ?: "")
        }
    }

    /** Cambia el estado de un pedido (PENDIENTE, PREPARADO o ENTREGADO). Solo administrador. */
    suspend fun cambiarEstado(uid: String, pedidoId: String, estado: String) {
        FirestorePaths.pedidos(uid).document(pedidoId).update("estado", estado).await()
    }
}
