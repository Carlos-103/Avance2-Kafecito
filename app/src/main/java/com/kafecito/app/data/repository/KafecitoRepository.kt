package com.kafecito.app.data.repository

import com.kafecito.app.data.model.Producto
import com.kafecito.app.data.model.Usuario
import com.kafecito.app.R

/**
 * Repositorio en memoria que gestiona los usuarios y productos de Kafecito.
 * Los datos se mantienen mientras la aplicación está abierta y se pierden al cerrarla.
 */
object KafecitoRepository {

    private val usuarios = mutableListOf(
        Usuario(1, "David", "admin@kafecito.com", "123456")
    )
    private var usuarioActual: Usuario? = null

    /** Registra una cuenta nueva.
     * Devuelve null si el registro fue exitoso o un mensaje si ocurrió un error. */
    fun registrar(nombre: String, correo: String, password: String): String? {
        if (usuarios.any { it.correo.equals(correo, ignoreCase = true) }) {
            return "Ya existe una cuenta con ese correo"
        }
        val nuevo = Usuario(usuarios.size + 1, nombre, correo, password)
        usuarios.add(nuevo)
        usuarioActual = nuevo
        return null
    }

    /** Inicia sesión y devuelve el usuario si las credenciales son correctas;
     * de lo contrario, devuelve null. */
    fun login(correo: String, password: String): Usuario? {
        val usuario = usuarios.find {
            it.correo.equals(correo, ignoreCase = true) && it.password == password
        }
        usuarioActual = usuario
        return usuario
    }

    fun cerrarSesion() { usuarioActual = null }

    fun nombreUsuarioActual(): String = usuarioActual?.nombre ?: "Invitado"

    fun correoUsuarioActual(): String = usuarioActual?.correo ?: ""

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
