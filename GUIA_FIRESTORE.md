# Guía rápida de Firestore (copiar y adaptar)

Todo cuelga de `usuarios/{uid}/...`. Las rutas ya están en `FirestorePaths.kt`:

```kotlin
FirestorePaths.uidActual()          // uid del usuario con sesión
FirestorePaths.direcciones(uid)     // usuarios/{uid}/direcciones
```

En los repositorios se usan funciones `suspend` y `.await()`
(`import kotlinx.coroutines.tasks.await`). Los ejemplos usan **Direccion**; para tu tarea
cambia la colección y el modelo.

## 1. Leer una lista

```kotlin
suspend fun listar(): List<Direccion> {
    val uid = FirestorePaths.uidActual()
    val snap = FirestorePaths.direcciones(uid).get().await()
    // Se copia el id del documento al objeto.
    return snap.documents.mapNotNull { doc ->
        doc.toObject(Direccion::class.java)?.copy(id = doc.id)
    }
}
```

Para ordenar (ej. pedidos del más nuevo al más viejo):
`FirestorePaths.pedidos(uid).orderBy("fecha", Query.Direction.DESCENDING).get().await()`
(`import com.google.firebase.firestore.Query`).

## 2. Agregar

```kotlin
suspend fun agregar(direccion: Direccion) {
    val uid = FirestorePaths.uidActual()
    val ref = FirestorePaths.direcciones(uid).document()      // Firestore genera el id
    ref.set(direccion.copy(id = ref.id)).await()
}
```

Si el id debe ser uno que tú decides (favoritos usa el id del producto):
`FirestorePaths.favoritos(uid).document(productoId.toString()).set(mapOf("productoId" to productoId)).await()`

## 3. Borrar

```kotlin
suspend fun borrar(id: String) {
    val uid = FirestorePaths.uidActual()
    FirestorePaths.direcciones(uid).document(id).delete().await()
}
```

## 4. Contar

```kotlin
suspend fun contar(): Int {
    val uid = FirestorePaths.uidActual()
    val resultado = FirestorePaths.direcciones(uid).count().get(AggregateSource.SERVER).await()
    return resultado.count.toInt()
}
```
(`import com.google.firebase.firestore.AggregateSource`)

## Cómo llamarlo desde la pantalla

Las funciones `suspend` se llaman desde un ViewModel con `viewModelScope.launch`.
Siempre con `try/catch`: sin internet o con reglas mal puestas lanzan excepciones.

```kotlin
class DireccionesViewModel : ViewModel() {
    var direcciones by mutableStateOf<List<Direccion>>(emptyList())
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init { cargar() }

    fun cargar() = viewModelScope.launch {
        try {
            direcciones = DireccionRepository.listar()
        } catch (e: Exception) {
            error = "No se pudieron cargar las direcciones"
        }
    }

    fun borrar(id: String) = viewModelScope.launch {
        try {
            DireccionRepository.borrar(id)
            cargar()
        } catch (e: Exception) {
            error = "No se pudo borrar"
        }
    }
}
```

En la pantalla: `val vm: DireccionesViewModel = viewModel()` y se usa `vm.direcciones`
en lugar de la lista de ejemplo.

## Errores comunes

- `PERMISSION_DENIED`: no hay sesión iniciada (entra con `admin@kafecito.com`) o las reglas
  no coinciden con la ruta que escribes. Las reglas están en `firestore.rules`.
- `No hay una sesion iniciada` (lo lanza `FirestorePaths.uidActual()`): abriste la pantalla
  sin haber iniciado sesión. Entra primero por el Login.
- `Default FirebaseApp is not initialized`: `app/google-services.json` no es el real
  (o no hiciste Sync y Build → Clean Project después del `git pull`).
- No devuelve nada: revisa que el modelo tenga **valores por defecto en todos los campos**
  (Firestore necesita un constructor vacío).
