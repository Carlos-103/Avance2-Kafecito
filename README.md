# Kafecito — Android (Etapa 3, Avance 2)

Proyecto Kotlin + Jetpack Compose basado en el Figma KAFECITO APP.

**Alcance del Avance 2:** integrar **Firebase Authentication** (correo y Google) y **Cloud Firestore**, de modo que los datos de la app queden guardados en la nube y asociados al usuario autenticado: favoritos, direcciones, métodos de pago y pedidos.

**Estado:** ✅ Firebase configurado y Authentication (correo y Google) funcionando. ⏳ Falta conectar Firestore en cada módulo (ver tabla de pendientes).

---

## Cómo abrirlo

1. Abrir la carpeta del proyecto desde Android Studio (**Open**) y esperar el Gradle Sync.
2. **⚠️ Importante (Error de Java):** Si al sincronizar sale el error _"Incompatible Gradle JVM version"_:
   - Ve a **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**.
   - En **Gradle JDK**, selecciona una versión compatible (**21**). Si no tienes ninguna, elige **"Download JDK..."** en ese mismo menú.
   - Dale a **Apply / OK** y vuelve a sincronizar con el ícono del elefantito (Sync Project).
3. Si Android Studio muestra _"Project update recommended"_ (AGP Upgrade Assistant), **ignóralo**: no es un error y actualizar puede romper el proyecto.
4. Ejecutar en un celular por USB (con depuración USB activada) y **con internet**: la app usa Firebase.
5. **Usuario de prueba** (ya existe en Firebase): `admin@kafecito.com` / `123456`. También se puede crear una cuenta nueva en "Crear cuenta" o entrar con Google.

> **No reemplazar `app/google-services.json`.** Ya es el real del proyecto de Firebase (`kafecito-456`). Si se sobrescribe con otro (o con el placeholder), Firebase deja de funcionar.

## Cómo encontrar lo que falta

Cada pendiente está en el código como comentario `TODO(ETIQUETA)` con las instrucciones paso a paso.
En Android Studio: **View → Tool Windows → TODO**, y buscar por la etiqueta (por ejemplo `TODO(CHECKOUT)`).

Los patrones de Firestore que se repiten (leer lista, agregar, borrar, contar) están en **`GUIA_FIRESTORE.md`**: copiar y adaptar.

---

## Qué ya funciona

- Todo lo del Avance 1: Welcome, Login y Registro, Home con buscador y categorías, Detalle de producto, Carrito con subtotal, IVA (13%) y total, y navegación con barra inferior.
- ✅ **`FIREBASE-CONFIG`**: proyecto de Firebase creado, app registrada, proveedores Correo y Google activados, Firestore creado con las reglas de `firestore.rules` publicadas y `google-services.json` real en el repo.
- ✅ **`AUTH-CORREO`**: registro y login con Firebase, creación de `usuarios/{uid}` con `rol: "CLIENTE"`, recuperar contraseña ("¿Olvidaste tu contraseña?"), mensajes de error en español, sesión persistente (la app abre directo en el Home si ya hay sesión) y cerrar sesión. El Home y el Perfil muestran el nombre real del usuario.
- ✅ **`AUTH-GOOGLE`**: botón de Google en Login y Registro (Credential Manager). Si es la primera vez, se crea `usuarios/{uid}`.
- Modelos nuevos: `Direccion`, `MetodoPago`, `Pedido` y `PerfilUsuario` (con rol, `"CLIENTE"` por defecto).
- Repositorios con las funciones declaradas (`FavoritosRepository`, `DireccionRepository`, `MetodoPagoRepository`, `PedidoRepository`). **Mientras no se implementen devuelven lista vacía o 0**, así la app no se cae. Todo cuelga de `usuarios/{uid}/...`.
- Pantallas con la interfaz armada y **datos de ejemplo**: Favoritos, Mis direcciones, Métodos de pago, Pagar y Mis pedidos.
- Navegación con todas las rutas: el menú del Perfil abre sus pantallas, "Ir a Pagar" llega a Pagar y la barra inferior es Home, Contacto, Favoritos, Perfil.

###

## Para conectar tu módulo a Firestore, sigue los pasos de **`GUIA_FIRESTORE.md`**.

## Qué falta (etiquetas)

| Etiqueta            | Dónde                                                                                   | Qué hay que hacer                                                                                                                                                                                                                                                        |
| ------------------- | --------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `TODO(FAVORITOS)`   | `FavoritosRepository.kt`, `FavoritosScreen.kt`, `ProductDetailScreen.kt`, `NavGraph.kt` | Corazón en el Detalle, pantalla Favoritos real y marcar o desmarcar en Firestore.                                                                                                                                                                                        |
| `TODO(DIRECCIONES)` | `DireccionRepository.kt`, `DireccionesScreen.kt`                                        | Listar, agregar y borrar direcciones, con validación del formulario.                                                                                                                                                                                                     |
| `TODO(PAGOS)`       | `MetodoPagoRepository.kt`, `MetodosPagoScreen.kt`                                       | Listar, agregar y borrar métodos de pago (simulado). Solo se guardan la marca y los últimos 4 dígitos, nunca el número completo.                                                                                                                                         |
| `TODO(CHECKOUT)`    | `PagarScreen.kt`, `PedidoRepository.kt`                                                 | Elegir "Recoger en tienda" o "A domicilio" (con una dirección guardada), elegir método de pago (incluida la opción fija "Efectivo en tienda"; manejar la lista vacía de tarjetas), mostrar el resumen y, al confirmar, crear el pedido en Firestore y vaciar el carrito. |
| `TODO(PEDIDOS)`     | `PedidoRepository.kt`, `PedidosScreen.kt`                                               | Historial desde Firestore, del más nuevo al más viejo. El botón "Repetir" ya está conectado en `NavGraph`.                                                                                                                                                               |
| `TODO(PERFIL)`      | `ProfileScreen.kt`                                                                      | Estadísticas reales (pedidos, favoritos, puntos). Regla de puntos: 1 punto por cada $1 del total (`PedidoRepository.puntosDe`).                                                                                                                                          |

Además, para la entrega del Avance 2:

- `TODO(PDF)`: **PDF de investigación (APA 7)** sobre Firebase aplicado al proyecto (sin él la nota base es 5).
- `TODO(VIDEO)`: **Video demostración** (máx. 15 min): registro, login con correo y con Google, favoritos, direcciones, métodos de pago, pagar, historial de pedidos y cerrar sesión.

---

## ⚠️ Advertencias (leer antes de programar)

- **No pasar el catálogo a Firestore.** El catálogo sigue **local** en el Avance 2; se pasará con el admin de la Entrega Final. Por eso `CHECKOUT` tampoco descuenta stock.
- **No guardar números reales de tarjeta.** Los métodos de pago son **simulados**: solo se guardan la marca y los últimos 4 dígitos, y no se procesa ningún cobro.
- **No editar ni borrar pedidos.** Los pedidos solo se pueden **crear y leer**; las reglas de Firestore bloquean lo demás (`PERMISSION_DENIED`).
- "Repetir" no modifica el pedido viejo: carga sus productos al carrito y al pagar se crea uno nuevo.
- **No cambiar el rol desde la app.** El rol de admin se asigna a mano en la consola de Firebase; las reglas lo impiden.
- **El carrito no se guarda en Firestore.** Vive en memoria y se pierde al cerrar la app; solo el pedido confirmado queda guardado.

---

## Firebase (proyecto `kafecito-456`)

- Todos los celulares usan el mismo proyecto mediante `app/google-services.json` (ya viene en el repo, no reemplazarlo).
- Para probar, entrar con el usuario de prueba `admin@kafecito.com` / `123456`. No hace falta configurar nada más.
- Como todos comparten ese usuario, verán los mismos datos. Para datos propios, registrarse con un correo distinto.
- Si quieren ver sus datos en Firebase (**Firestore → Datos**), pedirle acceso al dueño del repo.
- **Botón de Google (opcional, no es necesario para trabajar):** quien quiera probarlo debe pasarle su **SHA-1** al dueño del repo.
- Se obtiene con `./gradlew signingReport` (línea `SHA1:` de `Variant: debug`).
- El dueño lo agrega en Firebase (⚙ Configuración del proyecto → Tus apps → Agregar huella digital). No hay que volver a descargar el JSON

---

## Estructura

```
app/src/main/java/com/kafecito/app/
├── MainActivity.kt
├── navigation/NavGraph.kt          Rutas y barra inferior
├── data/model/                     Usuario, PerfilUsuario, Producto, ItemCarrito,
│                                   Direccion, MetodoPago, Pedido
├── data/repository/                KafecitoRepository (catálogo local), AuthRepository,
│                                   FavoritosRepository, DireccionRepository,
│                                   MetodoPagoRepository, PedidoRepository, FirestorePaths
├── viewmodel/                      LoginViewModel, CarritoViewModel
├── util/                           Validaciones, Formato
└── ui/
    ├── theme/                      Color.kt, Type.kt, Theme.kt
    ├── components/                 KafeBottomBar, KafeDarkField, KafeTopBar,
    │                               KafeFilaTarjeta, KafeGoogleButton
    └── screens/                    Welcome, Login, Register, Home, ProductDetail, Cart,
                                    Favoritos, Direcciones, MetodosPago, Pagar, Pedidos,
                                    Profile, Contact
firestore.rules                     Reglas de seguridad (ya publicadas en Firebase)
GUIA_FIRESTORE.md                   Patrones de Firestore para copiar y adaptar
```

## Trabajo en equipo (Git)

- Cada integrante trabaja en **su propia rama** (`git checkout -b nombre-tarea`), nunca directo en `main`.
- Al terminar: commit, push y Pull Request; el dueño del repositorio revisa y hace el merge.
- `NavGraph.kt` y `ProfileScreen.kt` los tocan varios módulos (`FAVORITOS`, `PERFIL`): hacer cambios **pequeños** y avisar en el grupo para evitar conflictos.
- **Orden de merge:**
  1. ✅ `FIREBASE-CONFIG` + `AUTH-CORREO` + `AUTH-GOOGLE` (ya hecho)
  2. `FAVORITOS`, `DIRECCIONES` y `PAGOS` (independientes entre sí, cualquier orden)
  3. `CHECKOUT`
  4. `PEDIDOS`
  5. `PERFIL`

### Responsables

| Etiqueta                                        | Responsable    | Estado   |
| ----------------------------------------------- | -------------- | -------- |
| `FIREBASE-CONFIG`, `AUTH-CORREO`, `AUTH-GOOGLE` | Dueño del repo | ✅ Hecho |
| `FAVORITOS`, `DIRECCIONES`                      |                | ⏳       |
| `PAGOS`, CHECKOUT                               |                | ⏳       |
| `PEDIDOS`, `PERFIL`                             |                | ⏳       |
| `PDF`                                           |                | ⏳       |
