package com.kafecito.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FavoritosRepository {

    private val db = FirebaseFirestore.getInstance()

    // Colección de favoritos del usuario actual (null si no hay sesión)
    private fun favoritosRef() = FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
        db.collection("usuarios").document(uid).collection("favoritos")
    }

    suspend fun listar(): List<Int> {
        val ref = favoritosRef() ?: return emptyList()
        return ref.get().await().documents.mapNotNull { it.id.toIntOrNull() }
    }

    suspend fun agregar(productoId: Int) {
        val ref = favoritosRef() ?: return
        ref.document(productoId.toString())
            .set(mapOf("productoId" to productoId))
            .await()
    }

    suspend fun quitar(productoId: Int) {
        val ref = favoritosRef() ?: return
        ref.document(productoId.toString()).delete().await()
    }

    suspend fun contar(): Int {
        val ref = favoritosRef() ?: return 0
        return ref.get().await().size()
    }

    suspend fun esFavorito(productoId: Int): Boolean {
        val ref = favoritosRef() ?: return false
        return ref.document(productoId.toString()).get().await().exists()
    }
}