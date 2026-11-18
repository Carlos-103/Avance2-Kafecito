package com.kafecito.app.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.kafecito.app.data.model.ItemCarrito
import com.kafecito.app.data.model.Producto

/**
 * Administra el carrito compartido entre las pantallas Home, Detalle y Carrito.
 * Se instancia una sola vez desde NavGraph.kt.
 */
class CarritoViewModel : ViewModel() {
    // Lista observable que permite actualizar la interfaz cuando cambia el carrito.
    private val _items = mutableStateListOf<ItemCarrito>()
    val items: List<ItemCarrito> get() = _items

    /**
     * Agrega un producto al carrito o aumenta su cantidad si ya existe.
     * Respeta el stock disponible.
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

    /** Elimina todos los productos del carrito, por ejemplo, al cerrar sesión. */
    fun vaciar() {
        _items.clear()
    }

    //Suma los importes de los productos sin incluir el IVA.
    val subtotal: Double get() = _items.sumOf { it.totalLinea }

    // Calcula el IVA (13 %) sobre el subtotal.
    val iva: Double get() = subtotal * TASA_IVA

    //Calcula el total a pagar sumando el subtotal y el IVA. 
    val total: Double get() = subtotal + iva

    private companion object {
        // Tasa del IVA aplicada al subtotal.
        const val TASA_IVA = 0.13
    }
}