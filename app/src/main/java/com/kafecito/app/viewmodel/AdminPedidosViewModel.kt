package com.kafecito.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafecito.app.data.model.Pedido
import com.kafecito.app.data.repository.PedidoRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

/**
 * Estado de la pantalla "Pedidos del local" (administrador).
 * Versión base que ya funciona: lista todos los pedidos y cambia su estado.
 *
 * TODO(ADMIN-PEDIDOS) - José: mejorar (mensaje si falla cambiar el estado, filtro por estado,
 * evitar toques repetidos mientras se guarda).
 */
class AdminPedidosViewModel : ViewModel() {
    var pedidos by mutableStateOf<List<Pedido>>(emptyList())
        private set
    var cargando by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            cargando = true
            error = null
            try {
                pedidos = withTimeout(15.seconds) { PedidoRepository.listarTodos() }
            } catch (e: Exception) {
                error = "No se pudieron cargar los pedidos"
            } finally {
                cargando = false
            }
        }
    }

    fun cambiarEstado(pedido: Pedido, estado: String) {
        viewModelScope.launch {
            try {
                withTimeout(15.seconds) { PedidoRepository.cambiarEstado(pedido.uid, pedido.id, estado) }
                pedidos = pedidos.map { if (it.id == pedido.id) it.copy(estado = estado) else it }
            } catch (e: Exception) {
                error = "No se pudo cambiar el estado"
            }
        }
    }
}
