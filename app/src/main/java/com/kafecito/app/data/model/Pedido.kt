package com.kafecito.app.data.model

/** Linea de un pedido. Se guarda una copia de nombre y precio al momento de la compra. */
data class PedidoItem(
    val productoId: Int = 0,
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 0
)

/** Documento usuarios/{uid}/pedidos/{id}. */
data class Pedido(
    val id: String = "",
    // Fecha en milisegundos (System.currentTimeMillis()).
    val fecha: Long = 0L,
    // "RECOGER" o "DOMICILIO".
    val tipoEntrega: String = "RECOGER",
    // Texto de la direccion elegida cuando es a domicilio (vacio si recoge en tienda).
    val direccion: String = "",
    val metodoPago: String = "",
    val items: List<PedidoItem> = emptyList(),
    val subtotal: Double = 0.0,
    val iva: Double = 0.0,
    val total: Double = 0.0
)

/** Convierte los items del carrito en items de pedido (para TODO(CHECKOUT)). */
fun List<ItemCarrito>.aPedidoItems(): List<PedidoItem> = map {
    PedidoItem(it.producto.id, it.producto.nombre, it.producto.precio, it.cantidad)
}
