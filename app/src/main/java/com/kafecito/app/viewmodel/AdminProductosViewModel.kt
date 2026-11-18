package com.kafecito.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafecito.app.data.model.Producto
import com.kafecito.app.data.repository.ProductoRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

/**
 * Estado de la pantalla de productos del administrador.
 * La pantalla (AdminProductosScreen) solo muestra estos datos y llama a estas funciones.
 *
 * Uso desde la pantalla:
 *   val vm: AdminProductosViewModel = viewModel()
 *   vm.productos            -> lista a mostrar
 *   vm.guardando / vm.error -> mostrar indicador de carga y mensaje de error
 *   vm.guardar(producto) { /* cerrar el formulario */ }
 *   vm.borrar(id)
 *   vm.siguienteId()        -> id para un producto nuevo
 */
class AdminProductosViewModel : ViewModel() {
    var productos by mutableStateOf<List<Producto>>(ProductoRepository.obtenerProductos())
        private set
    var guardando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun siguienteId(): Int = ProductoRepository.siguienteId()

    fun limpiarError() { error = null }

    /** Crea o edita. `onListo` se ejecuta solo si se guardó bien (por ejemplo, para cerrar el formulario). */
    fun guardar(producto: Producto, onListo: () -> Unit = {}) {
        viewModelScope.launch {
            guardando = true
            error = null
            try {
                withTimeout(15.seconds) { ProductoRepository.guardar(producto) }
                productos = ProductoRepository.obtenerProductos()
                onListo()
            } catch (e: Exception) {
                error = "No se pudo guardar el producto"
            } finally {
                guardando = false
            }
        }
    }

    fun borrar(id: Int) {
        viewModelScope.launch {
            guardando = true
            error = null
            try {
                withTimeout(15.seconds) { ProductoRepository.borrar(id) }
                productos = ProductoRepository.obtenerProductos()
            } catch (e: Exception) {
                error = "No se pudo borrar el producto"
            } finally {
                guardando = false
            }
        }
    }
}
