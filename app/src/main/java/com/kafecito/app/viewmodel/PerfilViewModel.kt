package com.kafecito.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafecito.app.data.repository.FavoritosRepository
import com.kafecito.app.data.repository.PedidoRepository
import kotlinx.coroutines.launch

/** Estadisticas reales del Perfil: pedidos, favoritos y puntos. */
class PerfilViewModel : ViewModel() {
    var pedidos by mutableStateOf(0)
        private set
    var favoritos by mutableStateOf(0)
        private set
    var puntos by mutableStateOf(0)
        private set

    fun cargar() {
        viewModelScope.launch {
            // Cada estadistica va en su propio try/catch: si una falla, las demas se muestran.
            try { pedidos = PedidoRepository.contar() } catch (e: Exception) { }
            try { favoritos = FavoritosRepository.contar() } catch (e: Exception) { }
            try { puntos = PedidoRepository.puntosTotales() } catch (e: Exception) { }
        }
    }
}
