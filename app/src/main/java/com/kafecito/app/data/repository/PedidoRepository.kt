package com.kafecito.app.data.repository

import com.kafecito.app.data.model.Pedido
import kotlinx.coroutines.tasks.await

/**
 * Pedidos del usuario: usuarios/{uid}/pedidos/{id}.
 * Mientras no se implemente cada funcion devuelve un valor seguro (lista vacia o 0)
 * para que la app no se caiga si otra pantalla ya la llama.
 */
object PedidoRepository {

    /** Regla de puntos: 1 punto por cada $1 del total (hacia abajo). Ya funciona. */
    fun puntosDe(total: Double): Int = total.toInt()

    /**
     * Guarda el pedido en usuarios/{uid}/pedidos y devuelve el id creado.
     * Quien llama debe vaciar el carrito despues (carritoVM.vaciar()).
     * Si falla lanza una excepcion, asi no se vacia el carrito sin haber guardado nada.
     */
    suspend fun crear(pedido: Pedido): String {
        require(pedido.items.isNotEmpty()) { "El pedido no tiene productos" }
        val uid = FirestorePaths.uidActual()
        val ref = FirestorePaths.pedidos(uid).document()   // Firestore genera el id
        ref.set(pedido.copy(id = ref.id)).await()
        return ref.id
    }

    // TODO(PEDIDOS): patron "leer lista" ordenada del mas nuevo al mas viejo:
    // pedidos(uid).orderBy("fecha", Query.Direction.DESCENDING)
    suspend fun listar(): List<Pedido> {
        return emptyList()
    }

    // TODO(PERFIL): patron "contar" (estadisticas del Perfil).
    suspend fun contar(): Int {
        return 0
    }
}