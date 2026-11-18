package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kafecito.app.ui.components.KafeHeader
import com.kafecito.app.ui.components.KafeTopBar
import com.kafecito.app.ui.theme.*

/**
 * Menú del administrador. Solo se llega aquí desde el Perfil si el rol es ADMIN.
 * (TODO(ROLES): las reglas de Firestore son las que protegen los datos de verdad.)
 */
@Composable
fun AdminPanelScreen(
    onAbrirProductos: () -> Unit,
    onAbrirPedidos: () -> Unit,
    onAbrirReportes: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(KafeBlack)) {
        KafeHeader { Text(text = "ADMINISTRACIÓN", style = MaterialTheme.typography.titleMedium) }
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            FilaAdmin(Icons.Filled.Inventory2, "Productos", onAbrirProductos)
            FilaAdmin(Icons.Filled.Receipt, "Pedidos del local", onAbrirPedidos)
            FilaAdmin(Icons.Filled.BarChart, "Reportes", onAbrirReportes)
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onCerrarSesion,
                colors = ButtonDefaults.buttonColors(containerColor = KafeWhite, contentColor = KafeBlack),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("Cerrar sesión") }
        }
    }
}
@Composable
private fun FilaAdmin(icono: ImageVector, texto: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = KafeWhite)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = texto, color = KafeWhite, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = KafeGray)
    }
}
