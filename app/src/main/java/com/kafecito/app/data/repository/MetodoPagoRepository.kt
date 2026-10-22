package com.kafecito.app.data.repository

import com.kafecito.app.data.model.MetodoPago

/**
 * Metodos de pago (simulados): usuarios/{uid}/metodosPago/{id}. Solo marca y ultimos 4 digitos.
 * Mientras no se implemente cada funcion devuelve un valor seguro (lista vacia)
 * para que la app no se caiga si otra pantalla ya la llama.
 */
object MetodoPagoRepository {

    // TODO(PAGOS): patron "leer lista".
    suspend fun listar(): List<MetodoPago> {
        return emptyList()
    }

    // TODO(PAGOS): patron "agregar". Validar que ultimos4 tenga exactamente 4 digitos.
    suspend fun agregar(metodo: MetodoPago) {
        // Pendiente.
    }

    // TODO(PAGOS): patron "borrar".
    suspend fun borrar(id: String) {
        // Pendiente.
    }
}
