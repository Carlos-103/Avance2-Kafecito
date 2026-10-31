package com.kafecito.app.data.repository

import android.util.Log
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Query
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

    // TODO(CHECKOUT): guardar el pedido (patron "agregar") y devolver el id creado.
    // Despues de guardarlo, quien llama debe vaciar el carrito (carritoVM.vaciar()).
    suspend fun crear(pedido: Pedido): String {
        Log.w("PedidoRepository", "crear() pendiente: TODO(CHECKOUT). El pedido NO se guardo.")
        return ""
    }

    /** Historial del usuario, del mas nuevo al mas viejo. */
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

    /** Cantidad de pedidos del usuario (estadisticas del Perfil). */
    suspend fun contar(): Int {
        val uid = FirestorePaths.uidActual()
        val resultado = FirestorePaths.pedidos(uid).count().get(AggregateSource.SERVER).await()
        return resultado.count.toInt()
    }

    /** Puntos acumulados: se aplica puntosDe() a cada pedido y se suman. */
    suspend fun puntosTotales(): Int = listar().sumOf { puntosDe(it.total) }
}
