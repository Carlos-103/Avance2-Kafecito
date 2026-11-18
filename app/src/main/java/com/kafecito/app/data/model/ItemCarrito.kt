package com.kafecito.app.data.model

/**
 * Representa un producto dentro del carrito y la cantidad solicitada.
 */
data class ItemCarrito(
    val producto: Producto,
    val cantidad: Int
) {

    val totalLinea: Double get() = producto.precio * cantidad
}
