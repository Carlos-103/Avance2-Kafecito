package com.kafecito.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import com.kafecito.app.data.model.Direccion
import com.kafecito.app.data.model.MetodoPago
import com.kafecito.app.data.model.Pedido
import com.kafecito.app.data.repository.DireccionRepository
import com.kafecito.app.data.repository.MetodoPagoRepository
import com.kafecito.app.data.repository.PedidoRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * Gestiona los métodos de pago, las direcciones guardadas
 * y la creación de pedidos durante el proceso de pago.
 */
class PagarViewModel : ViewModel() {

    var metodos by mutableStateOf<List<MetodoPago>>(emptyList())
        private set
    var direcciones by mutableStateOf<List<Direccion>>(emptyList())
        private set
    // Indica si se están cargando los métodos de pago y las direcciones.
    var cargando by mutableStateOf(true)
        private set
    // Indica si se está guardando un pedido para evitar envíos duplicados.
    var procesando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            // Cada lista se carga de forma independiente para que un error no impida utilizar la otra opción.
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

    /** Guarda el pedido en Firestore y ejecuta [onExito] únicamente si se guarda correctamente. */
    fun confirmar(pedido: Pedido, onExito: () -> Unit) {
        if (procesando) return
        procesando = true
        error = null
        viewModelScope.launch {
            try {
                // Crea el pedido y descuenta el stock en una sola transacción.
                withTimeout(TIEMPO_MAX_MS) { PedidoRepository.crearConStock(pedido) }
                onExito()
            } catch (e: TimeoutCancellationException) {
                error = "Sin conexión: no se pudo confirmar el pedido. Revisa tu internet."
            } catch (e: Exception) {
                Log.e("PagarViewModel", "No se pudo confirmar el pedido", e)
                error = mensajeDeError(e)
            } finally {
                procesando = false
            }
        }
    }

    /**
     * Convierte la excepción en un mensaje claro para el usuario.
     * El mensaje de "sin stock" puede venir envuelto en otra excepción,
     * por eso se revisa toda la cadena de causas.
     */
    private fun mensajeDeError(e: Throwable): String {
        var actual: Throwable? = e
        while (actual != null) {
            val texto = actual.message.orEmpty()
            val inicio = texto.indexOf(MENSAJE_SIN_STOCK)
            if (inicio >= 0) {
                // Ejemplo: "Sin stock suficiente de Capuccino"
                val linea = texto.substring(inicio).lineSequence().first().trim().trimEnd('.')
                return "$linea. Ajusta tu carrito e inténtalo de nuevo."
            }
            if (actual is FirebaseFirestoreException &&
                actual.code == FirebaseFirestoreException.Code.UNAVAILABLE
            ) {
                return "Sin conexión: no se pudo confirmar el pedido. Revisa tu internet."
            }
            actual = actual.cause
        }
        return "No se pudo confirmar el pedido. Inténtalo de nuevo."
    }
    // Tiempo máximo de espera para las operaciones de Firestore.
    private companion object {
        const val TIEMPO_MAX_MS = 15_000L
        // Texto con el que PedidoRepository.crearConStock avisa que falta stock.
        const val MENSAJE_SIN_STOCK = "Sin stock suficiente"
    }
}