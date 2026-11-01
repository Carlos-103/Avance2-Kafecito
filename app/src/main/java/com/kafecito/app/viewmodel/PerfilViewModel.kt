package com.kafecito.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafecito.app.data.repository.FavoritosRepository
import com.kafecito.app.data.repository.PedidoRepository
import kotlinx.coroutines.launch

/** Gestiona las estadísticas del perfil:
 * cantidad de pedidos, favoritos y puntos acumulados. */
class PerfilViewModel : ViewModel() {
    var pedidos by mutableStateOf(0)
        private set
    var favoritos by mutableStateOf(0)
        private set
    var puntos by mutableStateOf(0)
        private set

    // Cada estadística se carga de forma independiente para que un error no impida mostrar las demás.
    fun cargar() {
        viewModelScope.launch {
            try { pedidos = PedidoRepository.contar() } catch (e: Exception) { }
            try { favoritos = FavoritosRepository.contar() } catch (e: Exception) { }
            try { puntos = PedidoRepository.puntosTotales() } catch (e: Exception) { }
        }
    }
}
