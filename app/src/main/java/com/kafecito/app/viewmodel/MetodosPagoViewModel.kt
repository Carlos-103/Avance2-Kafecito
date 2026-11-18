package com.kafecito.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafecito.app.data.model.MetodoPago
import com.kafecito.app.data.repository.MetodoPagoRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * Gestiona los métodos de pago del usuario mediante Firestore.
 * Permite cargarlos, agregarlos y eliminarlos.
 */
class MetodosPagoViewModel : ViewModel() {

    var metodos by mutableStateOf<List<MetodoPago>>(emptyList())
        private set
    var error by mutableStateOf<String?>(null)
        private set
    // Indica si se está guardando un metodo de pago para evitar envíos duplicados.
    var guardando by mutableStateOf(false)
        private set

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            try {
                metodos = withTimeout(TIEMPO_MAX_MS) { MetodoPagoRepository.listar() }
            } catch (e: TimeoutCancellationException) {
                error = "Sin conexión: no se pudieron cargar los métodos de pago"
            } catch (e: Exception) {
                error = "No se pudieron cargar los métodos de pago"
            }
        }
    }

    /**
     * Valida y guarda un método de pago.
     * Devuelve true si inicia el proceso de guardado o false si los datos no son válidos
     * o ya hay una operación en curso.
     */
    fun agregar(marca: String, ultimos4: String): Boolean {
        if (guardando) return false
        val marcaLimpia = marca.trim()
        if (marcaLimpia.isEmpty()) {
            error = "Escribe la marca de la tarjeta"
            return false
        }
        if (ultimos4.length != 4 || !ultimos4.all { it.isDigit() }) {
            error = "Los últimos 4 dígitos deben ser exactamente 4 números"
            return false
        }
        error = null
        guardando = true
        viewModelScope.launch {
            try {
                withTimeout(TIEMPO_MAX_MS) {
                    MetodoPagoRepository.agregar(MetodoPago(marca = marcaLimpia, ultimos4 = ultimos4))
                    metodos = MetodoPagoRepository.listar()
                }
            } catch (e: TimeoutCancellationException) {
                error = "Sin conexión: no se pudo guardar el método de pago"
            } catch (e: Exception) {
                error = "No se pudo guardar el método de pago"
            } finally {
                guardando = false
            }
        }
        return true
    }

    fun borrar(id: String) {
        viewModelScope.launch {
            try {
                withTimeout(TIEMPO_MAX_MS) { MetodoPagoRepository.borrar(id) }
                metodos = metodos.filter { it.id != id }
                error = null
            } catch (e: TimeoutCancellationException) {
                error = "Sin conexión: no se pudo borrar el método de pago"
            } catch (e: Exception) {
                error = "No se pudo borrar el método de pago"
            }
        }
    }
    // Tiempo máximo de espera para las operaciones de Firestore.
    private companion object {
        const val TIEMPO_MAX_MS = 15_000L
    }
}