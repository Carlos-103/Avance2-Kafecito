package com.kafecito.app.data.model

/**
 * Representa un producto dentro del carrito y la cantidad solicitada.
 */
data class ItemCarrito(
    val producto: Producto,
    val cantidad: Int
) {
    // Calcula el subtotal de la línea según el precio y la cantidad.
    val totalLinea: Double get() = producto.precio * cantidad
}
