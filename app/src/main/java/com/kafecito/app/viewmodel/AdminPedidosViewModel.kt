package com.kafecito.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafecito.app.data.model.EstadoPedido
import com.kafecito.app.data.model.Pedido
import com.kafecito.app.data.repository.PedidoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

/**
 * Estado de la pantalla "Pedidos del local" (administrador).
 */
class AdminPedidosViewModel : ViewModel() {
    var pedidos by mutableStateOf<List<Pedido>>(emptyList())
        private set
    var filtroEstado by mutableStateOf<String?>(null)
        private set
    val pedidosFiltrados: List<Pedido>
        get() = pedidos.filter { filtroEstado == null || it.estado == filtroEstado }
    var cargando by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var actualizandoIds by mutableStateOf<Set<String>>(emptySet())
        private set

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            cargando = true
            error = null
            try {
                pedidos = withTimeout(15.seconds) { PedidoRepository.listarTodos() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = "No se pudieron cargar los pedidos"
            } finally {
                cargando = false
            }
        }
    }

    fun filtrarPorEstado(estado: String?) {
        require(estado == null || estado in EstadoPedido.todos) { "Estado de pedido no válido" }
        filtroEstado = estado
    }

    fun estaActualizando(pedido: Pedido): Boolean =
        clavePedido(pedido) in actualizandoIds

    fun cambiarEstado(pedido: Pedido, estado: String) {
        require(estado in EstadoPedido.todos) { "Estado de pedido no válido" }
        val clave = clavePedido(pedido)
        if (pedido.estado == estado || clave in actualizandoIds) return

        viewModelScope.launch {
            actualizandoIds = actualizandoIds + clave
            error = null
            try {
                withTimeout(15.seconds) { PedidoRepository.cambiarEstado(pedido.uid, pedido.id, estado) }
                pedidos = pedidos.map {
                    if (it.uid == pedido.uid && it.id == pedido.id) it.copy(estado = estado) else it
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = "No se pudo cambiar el estado"
            } finally {
                actualizandoIds = actualizandoIds - clave
            }
        }
    }

    private fun clavePedido(pedido: Pedido): String = "${pedido.uid}/${pedido.id}"
}
