package com.kafecito.app.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Centraliza las rutas de acceso a las colecciones y documentos de Firestore.
 * Los datos de cada usuario se almacenan dentro de usuarios/{uid}/...
 *
 * La instancia de Firestore se obtiene cuando se necesita,
 * evitando inicializarla al cargar este objeto.
 */
object FirestorePaths {
    private val db get() = FirebaseFirestore.getInstance()

    fun usuario(uid: String): DocumentReference = db.collection("usuarios").document(uid)
    fun favoritos(uid: String): CollectionReference = usuario(uid).collection("favoritos")
    fun direcciones(uid: String): CollectionReference = usuario(uid).collection("direcciones")
    fun metodosPago(uid: String): CollectionReference = usuario(uid).collection("metodosPago")
    fun pedidos(uid: String): CollectionReference = usuario(uid).collection("pedidos")

    /** Obtiene el ID del usuario con sesión iniciada. Lanza un error si no hay una sesión activa. */
    fun uidActual(): String =
        AuthRepository.uidActual() ?: error("No hay una sesion iniciada")
}
