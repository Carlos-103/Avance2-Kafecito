package com.kafecito.app.data.repository

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Query
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
        ref.set(pedido.copy(id = ref.id)).await()
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
}
