package com.kafecito.app.data.model

/** Linea de un pedido. Se guarda una copia de nombre y precio al momento de la compra. */
data class PedidoItem(
    val productoId: Int = 0,
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 0
)

/** Estados posibles de un pedido. Los cambia el administrador desde su panel. */
object EstadoPedido {
    const val PENDIENTE = "PENDIENTE"
    const val PREPARADO = "PREPARADO"
    const val ENTREGADO = "ENTREGADO"
    val todos = listOf(PENDIENTE, PREPARADO, ENTREGADO)
}

/** Representa un producto dentro de un pedido,
 * conservando su nombre y precio al momento de la compra. */
data class Pedido(
    val id: String = "",
    // ID del usuario que hizo el pedido. Lo necesita el administrador para cambiar su estado.
    val uid: String = "",
    // Estado del pedido: PENDIENTE, PREPARADO o ENTREGADO (ver EstadoPedido).
    val estado: String = EstadoPedido.PENDIENTE,
    // Fecha del pedido en milisegundos desde el 1 de enero de 1970.
    val fecha: Long = 0L,
    // Tipo de entrega: "RECOGER" o "DOMICILIO".
    val tipoEntrega: String = "RECOGER",
    // Dirección seleccionada para entregas a domicilio. Vacía si se recoge en tienda.
    val direccion: String = "",
    val metodoPago: String = "",
    val items: List<PedidoItem> = emptyList(),
    val subtotal: Double = 0.0,
    val iva: Double = 0.0,
    val total: Double = 0.0
)

/** Convierte los productos del carrito en los elementos que se guardarán en el pedido. */
fun List<ItemCarrito>.aPedidoItems(): List<PedidoItem> = map {
    PedidoItem(it.producto.id, it.producto.nombre, it.producto.precio, it.cantidad)
}
