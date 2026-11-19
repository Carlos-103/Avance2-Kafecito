package com.kafecito.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafecito.app.data.model.Pedido
import com.kafecito.app.data.model.Producto
import com.kafecito.app.data.repository.PedidoRepository
import com.kafecito.app.data.repository.ProductoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

/** Datos ya calculados que muestra la pantalla de reportes. */
data class ReportesData(
    val cantidadPedidos: Int,
    val totalVendido: Double,
    val masVendidos: List<Pair<String, Int>>,
    val stockBajo: List<Producto>,
    val pedidosPorEstado: List<Pair<String, Int>>
)

sealed interface ReportesUiState {
    data object Cargando : ReportesUiState
    data object Error : ReportesUiState
    data class Listo(val datos: ReportesData) : ReportesUiState
}

class ReportesViewModel : ViewModel() {

    private val _estado = MutableStateFlow<ReportesUiState>(ReportesUiState.Cargando)
    val estado: StateFlow<ReportesUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    /** Carga los pedidos y calcula los reportes. También sirve para reintentar. */
    fun cargar() {
        _estado.value = ReportesUiState.Cargando
        viewModelScope.launch {
            try {
                val pedidos = withTimeout(15.seconds) { PedidoRepository.listarTodos() }
                // Refresca el catálogo para que el stock bajo sea el actual.
                ProductoRepository.cargar()
                _estado.value = ReportesUiState.Listo(calcular(pedidos))
            } catch (e: TimeoutCancellationException) {
                _estado.value = ReportesUiState.Error
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = ReportesUiState.Error
            }
        }
    }

    private fun calcular(pedidos: List<Pedido>): ReportesData {
        // Top 5 de productos más vendidos (por unidades).
        val masVendidos = pedidos
            .flatMap { it.items }
            .groupBy { it.nombre }
            .mapValues { (_, items) -> items.sumOf { it.cantidad } }
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }

        // Productos con 3 unidades o menos, los más críticos primero.
        val stockBajo = ProductoRepository.obtenerProductos()
            .filter { it.stock <= 3 }
            .sortedBy { it.stock }

        // Pedidos por estado; los estados sin pedidos aparecen con 0.
        val porEstado = pedidos.groupBy { it.estado.toString() }
        val pedidosPorEstado = listOf("PENDIENTE", "PREPARADO", "ENTREGADO")
            .map { estado -> estado to (porEstado[estado]?.size ?: 0) }

        return ReportesData(
            cantidadPedidos = pedidos.size,
            totalVendido = pedidos.sumOf { it.total },
            masVendidos = masVendidos,
            stockBajo = stockBajo,
            pedidosPorEstado = pedidosPorEstado
        )
    }
}