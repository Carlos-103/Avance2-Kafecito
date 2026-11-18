package com.kafecito.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kafecito.app.data.repository.AuthRepository
import com.kafecito.app.data.repository.ProductoRepository
import com.kafecito.app.ui.components.KafeBottomBar
import com.kafecito.app.ui.screens.*
import com.kafecito.app.ui.theme.KafeBlack
import com.kafecito.app.ui.theme.KafeWhite
import com.kafecito.app.viewmodel.CarritoViewModel
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch

object Rutas {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CONTACTO = "contacto"
    const val PERFIL = "perfil"
    const val CARRITO = "carrito"
    const val FAVORITOS = "favoritos"
    const val DIRECCIONES = "direcciones"
    const val METODOS_PAGO = "metodos_pago"
    const val PAGAR = "pagar"
    const val PEDIDOS = "pedidos"
    const val DETALLE = "detalle/{productoId}"

    // Panel del administrador.
    const val INICIO = "inicio"

    const val ADMIN = "admin"
    const val ADMIN_PRODUCTOS = "admin_productos"
    const val ADMIN_PEDIDOS = "admin_pedidos"
    const val REPORTES = "reportes"

    fun detalle(productoId: Int) = "detalle/$productoId"
}

// Rutas en las que se muestra la barra de navegación inferior.
private val rutasConBarra = setOf(Rutas.HOME, Rutas.FAVORITOS, Rutas.CONTACTO, Rutas.PERFIL)

/**
 * Configura la navegación entre las pantallas de Kafecito.
 *
 * Flujo principal:  Welcome -> Login / Registro -> Home -> Detalle -> Carrito
 * Barra inferior:   Home | Favoritos | Contacto | Perfil
 */
@Composable
fun KafecitoNavGraph(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route

    // Comparte el mismo carrito entre las pantallas de Home, Detalle y Carrito.
    val carritoVM: CarritoViewModel = viewModel()

    // Si ya existe una sesión de Firebase, el usuario entra directamente al Home.
    val destinoInicial = remember {
        if (AuthRepository.uidActual() != null) Rutas.INICIO else Rutas.WELCOME
    }

    Scaffold(
        bottomBar = {
            if (rutaActual in rutasConBarra) {
                KafeBottomBar(rutaActual = rutaActual) { destino ->
                    navController.navigate(destino) {
                        popUpTo(Rutas.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = destinoInicial,
            modifier = Modifier.padding(padding)
        ) {

            composable(Rutas.WELCOME) {
                WelcomeScreen(
                    onIniciarSesion = { navController.navigate(Rutas.LOGIN) },
                    onCrearCuenta = { navController.navigate(Rutas.REGISTER) }
                )
            }
            composable(Rutas.INICIO) {
                LaunchedEffect(Unit) {
                    val destino = if (AuthRepository.esAdmin()) Rutas.ADMIN else Rutas.HOME
                    navController.navigate(destino) { popUpTo(Rutas.INICIO) { inclusive = true } }
                }
                Box(modifier = Modifier.fillMaxSize().background(KafeBlack), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KafeWhite)
                }
            }

            composable(Rutas.LOGIN) {
                val scope = rememberCoroutineScope()
                LoginScreen(
                    onLoginExitoso = {
                        scope.launch {
                            val destino = if (AuthRepository.esAdmin()) Rutas.ADMIN else Rutas.HOME
                            navController.navigate(destino) { popUpTo(Rutas.WELCOME) { inclusive = true } }
                        }
                    },
                    onAtras = { navController.popBackStack() }
                )
            }

            composable(Rutas.REGISTER) {
                RegisterScreen(
                    onCuentaCreada = {
                        navController.navigate(Rutas.HOME) { popUpTo(Rutas.WELCOME) { inclusive = true } }
                    },
                    onAtras = { navController.popBackStack() }
                )
            }

            composable(Rutas.HOME) {
                HomeScreen(
                    onAbrirCarrito = { navController.navigate(Rutas.CARRITO) },
                    onAbrirDetalle = { id -> navController.navigate(Rutas.detalle(id)) },
                    onAgregar = { producto -> carritoVM.agregar(producto) }
                )
            }

            composable(
                route = Rutas.DETALLE,
                arguments = listOf(navArgument("productoId") { type = NavType.IntType })
            ) { entry ->
                val id = entry.arguments?.getInt("productoId") ?: -1
                ProductDetailScreen(
                    producto = ProductoRepository.obtenerProducto(id),
                    onAtras = { navController.popBackStack() },
                    onAgregar = { producto, cantidad -> carritoVM.agregar(producto, cantidad) }
                )
            }

            composable(Rutas.CARRITO) {
                CartScreen(
                    carritoVM = carritoVM,
                    onAtras = { navController.popBackStack() },
                    onIrAPagar = { navController.navigate(Rutas.PAGAR) }
                )
            }

            composable(Rutas.PAGAR) {
                PagarScreen(
                    carritoVM = carritoVM,
                    onAtras = { navController.popBackStack() },
                    onPedidoCreado = {
                        navController.navigate(Rutas.PEDIDOS) { popUpTo(Rutas.HOME) { inclusive = false } }
                    }
                )
            }

            composable(Rutas.FAVORITOS) {
                FavoritosScreen(onAbrirDetalle = { id -> navController.navigate(Rutas.detalle(id)) })
            }

            composable(Rutas.DIRECCIONES) {
                DireccionesScreen(onAtras = { navController.popBackStack() })
            }

            composable(Rutas.METODOS_PAGO) {
                MetodosPagoScreen(onAtras = { navController.popBackStack() })
            }

            composable(Rutas.PEDIDOS) {
                PedidosScreen(
                    onAtras = { navController.popBackStack() },
                    // Vuelve a agregar al carrito los productos de un pedido anterior.
                    // Se respeta el stock disponible y después se abre la pantalla del carrito.
                    onRepetir = { pedido ->
                        pedido.items.forEach { item ->
                            ProductoRepository.obtenerProducto(item.productoId)?.let { carritoVM.agregar(it, item.cantidad) }
                        }
                        navController.navigate(Rutas.CARRITO)
                    }
                )
            }

            composable(Rutas.CONTACTO) { ContactScreen() }

            // ---- Panel del administrador ----
            // TODO(ROLES): ademas de ocultar la opcion, las reglas de Firestore son las que protegen.
            composable(Rutas.ADMIN) {
                AdminPanelScreen(
                    onAbrirProductos = { navController.navigate(Rutas.ADMIN_PRODUCTOS) },
                    onAbrirPedidos = { navController.navigate(Rutas.ADMIN_PEDIDOS) },
                    onAbrirReportes = { navController.navigate(Rutas.REPORTES) },
                    onCerrarSesion = {
                        AuthRepository.cerrarSesion()
                        carritoVM.vaciar()
                        navController.navigate(Rutas.WELCOME) { popUpTo(Rutas.ADMIN) { inclusive = true } }
                    }
                )
            }
            composable(Rutas.ADMIN_PRODUCTOS) { AdminProductosScreen(onAtras = { navController.popBackStack() }) }
            composable(Rutas.ADMIN_PEDIDOS) { AdminPedidosScreen(onAtras = { navController.popBackStack() }) }
            composable(Rutas.REPORTES) { ReportesScreen(onAtras = { navController.popBackStack() }) }

            composable(Rutas.PERFIL) {
                // TODO(ROLES): esAdmin() ya esta conectado; falta implementarla en AuthRepository.
                var esAdmin by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { esAdmin = AuthRepository.esAdmin() }
                ProfileScreen(
                    esAdmin = esAdmin,
                    onAbrirAdmin = { navController.navigate(Rutas.ADMIN) },
                    onAbrirPedidos = { navController.navigate(Rutas.PEDIDOS) },
                    onAbrirDirecciones = { navController.navigate(Rutas.DIRECCIONES) },
                    onAbrirMetodosPago = { navController.navigate(Rutas.METODOS_PAGO) },
                    onAbrirFavoritos = { navController.navigate(Rutas.FAVORITOS) },
                    // Cierra la sesión, vacía el carrito y regresa a la pantalla de bienvenida.
                    onCerrarSesion = {
                        AuthRepository.cerrarSesion()
                        carritoVM.vaciar()
                        navController.navigate(Rutas.WELCOME) {
                            popUpTo(Rutas.HOME) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
