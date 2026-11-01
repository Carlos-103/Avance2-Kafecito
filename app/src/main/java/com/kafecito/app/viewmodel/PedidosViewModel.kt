package com.kafecito.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafecito.app.data.model.Pedido
import com.kafecito.app.data.repository.PedidoRepository
import kotlinx.coroutines.launch

/**
 * Gestiona el historial de pedidos del usuario, ordenado del más reciente al más antiguo.
 */
class PedidosViewModel : ViewModel() {
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
                pedidos = PedidoRepository.listar()
            } catch (e: Exception) {
                error = "No se pudo cargar el historial"
            } finally {
                cargando = false
            }
        }
    }
}
