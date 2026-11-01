package com.kafecito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kafecito.app.data.repository.AuthRepository
import com.kafecito.app.ui.components.KafeHeader
import com.kafecito.app.ui.theme.*
import com.kafecito.app.viewmodel.PerfilViewModel

/**
 * Muestra los datos del usuario, sus estadísticas y las opciones
 * para gestionar su cuenta y cerrar sesión.
 */
@Composable
fun ProfileScreen(
    onAbrirPedidos: () -> Unit,
    onAbrirDirecciones: () -> Unit,
    onAbrirMetodosPago: () -> Unit,
    onAbrirFavoritos: () -> Unit,
    onCerrarSesion: () -> Unit,
    vm: PerfilViewModel = viewModel()
) {
    // Actualiza las estadísticas cada vez que se abre la pantalla de perfil.
    LaunchedEffect(Unit) { vm.cargar() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KafeBlack)
    ) {
        KafeHeader {
            Text(text = "PERFIL", style = MaterialTheme.typography.titleMedium)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(KafeChip),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Person, contentDescription = null) }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = AuthRepository.nombreActual(),
                        color = KafeWhite,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = AuthRepository.correoActual(),
                        color = KafeGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Muestra la cantidad de pedidos, favoritos y puntos acumulados del usuario.
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Estadistica(valor = vm.pedidos, etiqueta = "Pedidos")
                Estadistica(valor = vm.favoritos, etiqueta = "Favoritos")
                Estadistica(valor = vm.puntos, etiqueta = "Puntos")
            }

            Spacer(modifier = Modifier.height(20.dp))

            FilaMenu(Icons.Filled.Receipt, "Mis pedidos", onAbrirPedidos)
            FilaMenu(Icons.Filled.Favorite, "Favoritos", onAbrirFavoritos)
            FilaMenu(Icons.Filled.LocationOn, "Mis direcciones", onAbrirDirecciones)
            FilaMenu(Icons.Filled.CreditCard, "Métodos de pago", onAbrirMetodosPago)

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
private fun Estadistica(valor: Int, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = valor.toString(), color = KafeWhite, style = MaterialTheme.typography.titleLarge)
        Text(text = etiqueta, color = KafeGray, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun FilaMenu(icono: ImageVector, texto: String, onClick: () -> Unit) {
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
