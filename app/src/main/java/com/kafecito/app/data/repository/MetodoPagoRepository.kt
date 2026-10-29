package com.kafecito.app.data.repository

import com.kafecito.app.data.model.MetodoPago
import kotlinx.coroutines.tasks.await

/**
 * Metodos de pago (simulados): usuarios/{uid}/metodosPago/{id}. Solo marca y ultimos 4 digitos.
 */
object MetodoPagoRepository {

    /** Devuelve los metodos de pago guardados del usuario con sesion. */
    suspend fun listar(): List<MetodoPago> {
        val uid = FirestorePaths.uidActual()
        val snap = FirestorePaths.metodosPago(uid).get().await()
        // Se copia el id del documento al objeto.
        return snap.documents
            .mapNotNull { doc -> doc.toObject(MetodoPago::class.java)?.copy(id = doc.id) }
            .sortedBy { it.marca.lowercase() }
    }

    /**
     * Guarda un metodo de pago. Valida marca y que ultimos4 tenga exactamente 4 digitos.
     * Se escribe un mapa con solo los campos necesarios (nunca el numero completo).
     */
    suspend fun agregar(metodo: MetodoPago) {
        val marca = metodo.marca.trim()
        val ultimos4 = metodo.ultimos4.trim()
        require(marca.isNotEmpty()) { "La marca no puede estar vacia" }
        require(ultimos4.length == 4 && ultimos4.all { it.isDigit() }) {
            "Los ultimos4 deben ser exactamente 4 digitos"
        }
        val uid = FirestorePaths.uidActual()
        val ref = FirestorePaths.metodosPago(uid).document()   // Firestore genera el id
        ref.set(mapOf("id" to ref.id, "marca" to marca, "ultimos4" to ultimos4)).await()
    }

    /** Borra un metodo de pago por su id. */
    suspend fun borrar(id: String) {
        val uid = FirestorePaths.uidActual()
        FirestorePaths.metodosPago(uid).document(id).delete().await()
    }
}
