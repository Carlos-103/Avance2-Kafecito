package com.kafecito.app.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kafecito.app.navigation.Rutas
import com.kafecito.app.ui.theme.KafeBlack
import com.kafecito.app.ui.theme.KafeGray
import com.kafecito.app.ui.theme.KafeWhite

// Representa una opción de navegación con su ruta e ícono.
private data class TabItem(val ruta: String, val icono: ImageVector)

// Opciones disponibles en la barra de navegación inferior: menú, contacto y perfil.
private val tabs = listOf(
    TabItem(Rutas.HOME, Icons.Filled.Receipt),
    TabItem(Rutas.CONTACTO, Icons.Filled.Headset),
    TabItem(Rutas.FAVORITOS, Icons.Filled.Favorite),
    TabItem(Rutas.PERFIL, Icons.Filled.Person)
)
/**
 * Barra inferior que permite navegar entre Home,
 * Contacto y Perfil, resaltando la ruta seleccionada.
 */
@Composable
fun KafeBottomBar(rutaActual: String?, onTabSeleccionado: (String) -> Unit) {
    NavigationBar(
        modifier = Modifier.height(64.dp),
        containerColor = KafeWhite
    ) {
        tabs.forEach { tab ->
            NavigationBarItem(
                // Indica si esta opción corresponde a la pantalla actual.
                selected = rutaActual == tab.ruta,
                onClick = { onTabSeleccionado(tab.ruta) },
                icon = { Icon(tab.icono, contentDescription = tab.ruta) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = KafeBlack,
                    unselectedIconColor = KafeGray,
                    indicatorColor = KafeWhite
                )
            )
        }
    }
}
