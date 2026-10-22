package com.kafecito.app.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.kafecito.app.data.model.ItemCarrito
import com.kafecito.app.data.model.Producto

/**
 * Administra el estado del carrito compartido entre Home,
 * Detalle y Carrito. Se crea una sola vez en NavGraph.kt.
 */
class CarritoViewModel : ViewModel() {
    // Lista observable que actualiza la interfaz cuando cambia el carrito.
    private val _items = mutableStateListOf<ItemCarrito>()
    val items: List<ItemCarrito> get() = _items

    /**
     * Agrega un producto al carrito o aumenta su cantidad
     * si ya existe uno con el mismo ID.
     */
    fun agregar(producto: Producto, cantidad: Int = 1) {
        val indice = _items.indexOfFirst { it.producto.id == producto.id }
        val actual = if (indice >= 0) _items[indice].cantidad else 0
        val nueva = (actual + cantidad).coerceAtMost(producto.stock)
        if (nueva <= actual) return
        if (indice >= 0) {
            _items[indice] = _items[indice].copy(cantidad = nueva)
        } else {
            _items.add(ItemCarrito(producto, nueva))
        }
    }

    /** Cambia la cantidad de un producto ya agregado, respetando el stock. */
    fun cambiarCantidad(productoId: Int, nuevaCantidad: Int) {
        val indice = _items.indexOfFirst { it.producto.id == productoId }
        if (indice < 0) return
        val item = _items[indice]
        when {
            // Si la cantidad llega a cero, elimina el producto del carrito.
            nuevaCantidad <= 0 -> quitar(productoId)
            // Impide superar las unidades disponibles en inventario
            nuevaCantidad > item.producto.stock -> Unit
            else -> _items[indice] = item.copy(cantidad = nuevaCantidad)
        }
    }

    /** Quita un producto del carrito. */
    fun quitar(productoId: Int) {
        _items.removeAll { it.producto.id == productoId }
    }

    /** Vacía el carrito (se usa al cerrar sesión). */
    fun vaciar() {
        _items.clear()
    }

    /** Suma de todas las líneas, sin IVA. */
    val subtotal: Double get() = _items.sumOf { it.totalLinea }

    /** IVA = 13 % del subtotal. */
    val iva: Double get() = subtotal * TASA_IVA

    /** Total a pagar = subtotal + IVA. */
    val total: Double get() = subtotal + iva

    private companion object {
        // Tasa del IVA aplicada al subtotal.
        const val TASA_IVA = 0.13
    }
}