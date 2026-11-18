package com.kafecito.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kafecito.app.data.model.Direccion
import kotlinx.coroutines.tasks.await

/**
 * Gestiona las direcciones guardadas por cada usuario en Firestore.
 * Cada dirección se almacena en su propio documento, cuyo ID genera Firestore.
 *
 * Las operaciones lanzan excepciones si no hay una sesión iniciada
 * o si ocurre algún problema al comunicarse con Firestore.
 */
object DireccionRepository {

    private val db = FirebaseFirestore.getInstance()

    // Obtiene la colección de direcciones correspondiente al usuario que inició sesión.
    private fun direccionesRef(): CollectionReference {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("No hay sesión iniciada")
        return db.collection("usuarios").document(uid).collection("direcciones")
    }

    suspend fun listar(): List<Direccion> {
        return direccionesRef()
            .orderBy("creadoEn", Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { it.toDireccion() }
    }

    suspend fun agregar(alias: String, calle: String, ciudad: String): Direccion {
        val ref = direccionesRef().document() // Firestore genera automáticamente el ID de la dirección.
        val datos = mapOf(
            "alias" to alias,
            "calle" to calle,
            "ciudad" to ciudad,
            "creadoEn" to FieldValue.serverTimestamp()
        )
        ref.set(datos).await()
        return Direccion(id = ref.id, alias = alias, calle = calle, ciudad = ciudad)
    }

    suspend fun borrar(id: String) {
        direccionesRef().document(id).delete().await()
    }

    // Obtiene los datos de una dirección. Si falta algún campo obligatorio, la descarta.
    private fun DocumentSnapshot.toDireccion(): Direccion? {
        val alias = getString("alias") ?: return null
        val calle = getString("calle") ?: return null
        val ciudad = getString("ciudad") ?: return null
        return Direccion(id = id, alias = alias, calle = calle, ciudad = ciudad)
    }
    // Actualiza los datos de una dirección existente y registra la fecha del cambio.
    suspend fun actualizar(id: String, alias: String, calle: String, ciudad: String): Direccion {
        direccionesRef().document(id).update(
            mapOf(
                "alias" to alias,
                "calle" to calle,
                "ciudad" to ciudad,
                "actualizadoEn" to FieldValue.serverTimestamp()
            )
        ).await()
        return Direccion(id = id, alias = alias, calle = calle, ciudad = ciudad)
    }
}