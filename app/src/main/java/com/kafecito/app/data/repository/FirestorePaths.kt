package com.kafecito.app.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Rutas de Firestore. Todo cuelga de usuarios/{uid}/...
 * Las instancias se piden al usarlas, asi la app no falla al abrir si Firebase aun no esta configurado.
 */
object FirestorePaths {
    private val db get() = FirebaseFirestore.getInstance()

    fun usuario(uid: String): DocumentReference = db.collection("usuarios").document(uid)
    fun favoritos(uid: String): CollectionReference = usuario(uid).collection("favoritos")
    fun direcciones(uid: String): CollectionReference = usuario(uid).collection("direcciones")
    fun metodosPago(uid: String): CollectionReference = usuario(uid).collection("metodosPago")
    fun pedidos(uid: String): CollectionReference = usuario(uid).collection("pedidos")

    /** uid del usuario con sesion iniciada. Lanza error si nadie ha iniciado sesion. */
    fun uidActual(): String =
        AuthRepository.uidActual() ?: error("No hay una sesion iniciada")
}
