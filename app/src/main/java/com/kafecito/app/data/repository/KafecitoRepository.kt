package com.kafecito.app.data.repository

import com.kafecito.app.data.model.Producto
import com.kafecito.app.R

/**
 * Repositorio en memoria que gestiona los usuarios y productos de Kafecito.
 * Los datos se mantienen mientras la aplicación está abierta y se pierden al cerrarla.
 */
object KafecitoRepository {

    val categorias = listOf("Café", "Desayuno", "Postres", "Bebidas")

}
