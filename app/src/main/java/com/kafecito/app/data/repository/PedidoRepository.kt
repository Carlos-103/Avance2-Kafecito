package com.kafecito.app.data.repository

import android.util.Log
import com.kafecito.app.data.model.Pedido

/**
 * Pedidos del usuario: usuarios/{uid}/pedidos/{id}.
 * Mientras no se implemente cada funcion devuelve un valor seguro (lista vacia o 0)
 * para que la app no se caiga si otra pantalla ya la llama.
 */
object PedidoRepository {

    /** Regla de puntos: 1 punto por cada $1 del total (hacia abajo). Ya funciona. */
    fun puntosDe(total: Double): Int = total.toInt()

    // TODO(CHECKOUT): guardar el pedido (patron "agregar") y devolver el id creado.
    // Despues de guardarlo, quien llama debe vaciar el carrito (carritoVM.vaciar()).
    suspend fun crear(pedido: Pedido): String {
        Log.w("PedidoRepository", "crear() pendiente: TODO(CHECKOUT). El pedido NO se guardo.")
        return ""
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
