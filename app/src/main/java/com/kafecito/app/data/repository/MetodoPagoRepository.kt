package com.kafecito.app.data.repository

import com.kafecito.app.data.model.MetodoPago
import kotlinx.coroutines.tasks.await

/**
 * Gestiona los métodos de pago de cada usuario en Firestore.
 * Solo almacena la marca y los últimos cuatro dígitos de la tarjeta.
 */
object MetodoPagoRepository {

    /** Obtiene los métodos de pago guardados del usuario con sesión iniciada. */
    suspend fun listar(): List<MetodoPago> {
        val uid = FirestorePaths.uidActual()
        val snap = FirestorePaths.metodosPago(uid).get().await()
        // Se copia el id del documento al objeto.
        return snap.documents
            .mapNotNull { doc -> doc.toObject(MetodoPago::class.java)?.copy(id = doc.id) }
            .sortedBy { it.marca.lowercase() }
    }

    /**
     * Guarda un metodo de pago después de validar la marca y los últimos cuatro dígitos.
     * Solo almacena los datos necesarios, nunca el número completo de la tarjeta.
     */
    suspend fun agregar(metodo: MetodoPago) {
        val marca = metodo.marca.trim()
        val ultimos4 = metodo.ultimos4.trim()
        require(marca.isNotEmpty()) { "La marca no puede estar vacia" }
        require(ultimos4.length == 4 && ultimos4.all { it.isDigit() }) {
            "Los ultimos4 deben ser exactamente 4 digitos"
        }
        val uid = FirestorePaths.uidActual()
        val ref = FirestorePaths.metodosPago(uid).document()   // Firestore genera automáticamente el ID del documento.
        ref.set(mapOf("id" to ref.id, "marca" to marca, "ultimos4" to ultimos4)).await()
    }

    /** Elimina un metodo de pago utilizando el ID de su documento en Firestore. */
    suspend fun borrar(id: String) {
        val uid = FirestorePaths.uidActual()
        FirestorePaths.metodosPago(uid).document(id).delete().await()
    }
}
