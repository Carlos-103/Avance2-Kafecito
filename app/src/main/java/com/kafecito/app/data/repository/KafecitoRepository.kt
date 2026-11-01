package com.kafecito.app.data.repository

import com.kafecito.app.data.model.Producto
import com.kafecito.app.R

/**
 * Repositorio en memoria que gestiona los usuarios y productos de Kafecito.
 * Los datos se mantienen mientras la aplicación está abierta y se pierden al cerrarla.
 */
object KafecitoRepository {

    val categorias = listOf("Café", "Desayuno", "Postres", "Bebidas")

    private val productos = listOf(
        Producto(1, "Capuccino", "Espresso con leche vaporizada y espuma", 2.50, 4, "Café", R.drawable.cappucino),
        Producto(2, "Café Americano", "Espresso diluido en agua caliente", 2.00, 8, "Café", R.drawable.cafe_americano),
        Producto(3, "Pastel de Chocolate", "Pastel suave de chocolate", 3.00, 2, "Postres",R.drawable.pastel_chocolate),
        Producto(4, "Sprite", "Gaseosa", 1.00, 5, "Bebidas", R.drawable.sprite),
        Producto(5, "Desayuno tipico", "Frijoles molidos, huevos y platanos fritos", 7.00, 5, "Desayuno",R.drawable.desayuno_tipico)

    )

    fun obtenerProductos(categoria: String? = null): List<Producto> =
        if (categoria == null) productos else productos.filter { it.categoria == categoria }

    fun obtenerProducto(id: Int): Producto? = productos.find { it.id == id }
}
