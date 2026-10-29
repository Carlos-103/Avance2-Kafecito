package com.kafecito.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafecito.app.data.model.Direccion
import com.kafecito.app.data.model.MetodoPago
import com.kafecito.app.data.model.Pedido
import com.kafecito.app.data.repository.DireccionRepository
import com.kafecito.app.data.repository.MetodoPagoRepository
import com.kafecito.app.data.repository.PedidoRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/** Estado de la pantalla Pagar: metodos y direcciones guardados + creacion del pedido. */
class PagarViewModel : ViewModel() {

    var metodos by mutableStateOf<List<MetodoPago>>(emptyList())
        private set
    var direcciones by mutableStateOf<List<Direccion>>(emptyList())
        private set
    // true mientras se cargan metodos y direcciones.
    var cargando by mutableStateOf(true)
        private set
    // true mientras se guarda el pedido (evita crear dos pedidos con doble toque).
    var procesando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            // Cada lista se carga por separado: si una falla, la otra y el pago en efectivo siguen funcionando.
            try {
                metodos = withTimeout(TIEMPO_MAX_MS) { MetodoPagoRepository.listar() }
            } catch (e: Exception) {
                error = "No se pudieron cargar tus métodos de pago. Puedes pagar en efectivo."
            }
            try {
                direcciones = withTimeout(TIEMPO_MAX_MS) { DireccionRepository.listar() }
            } catch (e: Exception) {
                error = "No se pudieron cargar tus direcciones."
            }
            cargando = false
        }
    }

    /** Guarda el pedido en Firestore. Llama a [onExito] solo si se guardo correctamente. */
    fun confirmar(pedido: Pedido, onExito: () -> Unit) {
        if (procesando) return
        procesando = true
        error = null
        viewModelScope.launch {
            try {
                withTimeout(TIEMPO_MAX_MS) { PedidoRepository.crear(pedido) }
                onExito()
            } catch (e: TimeoutCancellationException) {
                error = "Sin conexión: no se pudo confirmar el pedido. Revisa tu internet."
            } catch (e: Exception) {
                error = "No se pudo confirmar el pedido. Inténtalo de nuevo."
            } finally {
                procesando = false
            }
        }
    }

    private companion object {
        const val TIEMPO_MAX_MS = 15_000L
    }
}