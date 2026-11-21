# Kafecito ☕

Aplicación móvil para una cafetería, desarrollada en Kotlin con Android Studio y Jetpack Compose.

## Descripción

Kafecito es una aplicación móvil para consultar productos de una cafetería y gestionar pedidos mediante una interfaz sencilla e intuitiva. Existen dos tipos de usuario: el cliente, que compra y gestiona su perfil, y el administrador, que gestiona productos y pedidos del local. Los datos se guardan en la nube con Firebase.

## Tecnologías utilizadas

* Kotlin
* Android Studio
* Jetpack Compose
* Material 3
* Firebase Authentication
* Cloud Firestore
* Gradle
* Git y GitHub

## Funcionalidades

* Estructura inicial de la aplicación.
* Interfaz de usuario y componentes reutilizables.
* Pantallas de inicio de sesión y registro.
* Autenticación con Firebase: correo y contraseña, e inicio de sesión con Google.
* Recuperación de contraseña y sesión persistente.
* Visualización del catálogo (cargado desde Firestore) y detalles de productos.
* Navegación entre las principales pantallas.
* Carrito de compras: agregar productos, modificar cantidades, eliminar productos y vaciar el carrito, con contador sobre el ícono.
* Cálculo del subtotal, IVA del 13 % y total del pedido.
* Favoritos guardados en Firestore.
* Direcciones: agregar, editar y borrar, con validación del formulario.
* Métodos de pago simulados (solo se guardan la marca y los últimos 4 dígitos).
* Pago del pedido: recoger en tienda o a domicilio, con tarjeta guardada o efectivo.
* Control de stock: se descuenta al confirmar el pedido y se avisa si no hay existencias suficientes.
* Historial de pedidos con su estado (pendiente, preparado o entregado) y opción de repetir un pedido.
* Perfil con estadísticas: pedidos, favoritos y puntos.
* Rol de administrador con inicio propio, separado del de los clientes.
* Gestión de productos (administrador): crear, editar y borrar, con validación e imagen opcional mediante un enlace.
* Pedidos del local (administrador): lista de pedidos de todos los clientes, filtro por estado y cambio de estado.
* Reportes (administrador): pedidos realizados, total vendido, más vendidos, stock bajo y pedidos por estado.

## Requisitos

* Android Studio compatible con proyectos Kotlin y Jetpack Compose.
* JDK 21.
* Gradle 8.7
* SDK de Android compatible con la configuración del proyecto.
* Emulador de Android o dispositivo físico compatible.
* Conexión a internet (la aplicación usa Firebase).

## Instalación y ejecución

1. Clonar el repositorio.
2. Abrir el proyecto en Android Studio.
3. Esperar a que finalice la sincronización de Gradle.
4. Ejecutar la aplicación en un emulador o dispositivo Android compatible.
5. Iniciar sesión con un usuario de prueba (ver más abajo), crear una cuenta nueva desde **Crear cuenta** o iniciar sesión con Google. Las cuentas nuevas son de cliente.

> **Importante:** no reemplazar el archivo `app/google-services.json`, ya que contiene la configuración real del proyecto de Firebase.

## Usuarios de prueba

| Rol | Correo             |
|---|--------------------|---|
| Cliente | david@hotmail.com  |
| Administrador | admin@kafecito.com |

> El rol de administrador no se puede obtener desde la aplicación: se asigna manualmente en la consola de Firebase. Las reglas de seguridad de Firestore están en `firestore.rules`.

## Mock-ups

Diseño de la aplicación en Figma (pantallas de cliente y de administrador):
https://www.figma.com/design/8qCOYIM8vNzCwimN1Lic9d/KAFECITO-APP?node-id=1-2&t=jcMVk0lkuoT2NcLX-1

## Equipo de trabajo

* Carlos David Martínez Ventura — MV120141
* Carlos Alberto Alfaro Espinoza — AE220430
* Ricardo José Aguillón González — AG242725
* Kevin Antonio Flores Alvarado — FA251339
* José Antonio González Oliva — GO231492

## Licencia

Este proyecto se distribuye bajo la licencia **[COMPLETAR: Creative Commons, por ejemplo CC BY-NC-SA 4.0]**. El texto completo está en el archivo [`LICENSE`](LICENSE).