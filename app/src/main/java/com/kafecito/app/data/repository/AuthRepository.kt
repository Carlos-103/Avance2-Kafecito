package com.kafecito.app.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.kafecito.app.data.model.PerfilUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * Autenticacion con Firebase (correo y Google).
 * Convencion (igual que KafecitoRepository.registrar del Avance 1):
 * las funciones devuelven null si todo salio bien, o el mensaje de error en español.
 *
 * Al registrarse o iniciar sesion se asegura que exista usuarios/{uid} en Firestore
 * con rol "CLIENTE" (las reglas solo permiten crearlo con ese rol).
 */
object AuthRepository {

    private const val TAG = "AuthRepository"
    private const val ROL_CLIENTE = "CLIENTE"

    private val auth get() = FirebaseAuth.getInstance()

    /** Usuario de Firebase o null. No lanza error aunque Firebase no este configurado. */
    private val usuarioActual: FirebaseUser?
        get() = try {
            FirebaseAuth.getInstance().currentUser
        } catch (e: Exception) {
            null
        }

    // ------------------------------------------------------------------------
    // SESION
    // ------------------------------------------------------------------------

    /** uid del usuario actual o null si no hay sesion. */
    fun uidActual(): String? = usuarioActual?.uid

    /** Nombre para mostrar en pantalla (Home, Perfil). "Invitado" si no hay sesion. */
    fun nombreActual(): String = usuarioActual?.let { usuario ->
        usuario.displayName?.takeIf { it.isNotBlank() } ?: usuario.email?.substringBefore("@")
    } ?: "Invitado"

    /** Correo del usuario actual o vacio si no hay sesion. */
    fun correoActual(): String = usuarioActual?.email ?: ""

    /** Cierra la sesion de Firebase. */
    fun cerrarSesion() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo cerrar sesion", e)
        }
    }

    // ------------------------------------------------------------------------
    // CORREO
    // ------------------------------------------------------------------------

    /** Crea la cuenta con correo y contraseña, guarda el nombre y crea usuarios/{uid}. */
    suspend fun registrar(nombre: String, correo: String, password: String): String? {
        return try {
            val resultado = auth.createUserWithEmailAndPassword(correo, password).await()
            val usuario = resultado.user ?: return "No se pudo crear la cuenta. Intenta de nuevo"
            usuario.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(nombre).build()
            ).await()
            asegurarPerfil(usuario, nombre)
            null
        } catch (e: Exception) {
            relanzarSiCancelado(e)
            traducirError(e)
        }
    }

    /** Inicia sesion con correo y contraseña. */
    suspend fun login(correo: String, password: String): String? {
        return try {
            val resultado = auth.signInWithEmailAndPassword(correo, password).await()
            resultado.user?.let { asegurarPerfil(it) }
            null
        } catch (e: Exception) {
            relanzarSiCancelado(e)
            traducirError(e)
        }
    }

    /** Envia el correo para restablecer la contraseña. */
    suspend fun recuperarPassword(correo: String): String? {
        return try {
            auth.sendPasswordResetEmail(correo).await()
            null
        } catch (e: Exception) {
            relanzarSiCancelado(e)
            traducirError(e)
        }
    }

    // ------------------------------------------------------------------------
    // GOOGLE
    // ------------------------------------------------------------------------

    /**
     * Flujo completo de Google: muestra el selector de cuentas, obtiene el idToken
     * y entra a Firebase. Requiere una Activity como context (usar LocalContext.current).
     */
    suspend fun iniciarConGoogle(context: Context): String? {
        return try {
            val idToken = obtenerIdTokenGoogle(context)
            loginConGoogle(idToken)
        } catch (e: Exception) {
            relanzarSiCancelado(e)
            traducirError(e)
        }
    }

    /** idToken de Google -> credencial de Firebase. Si es la primera vez, crea usuarios/{uid}. */
    suspend fun loginConGoogle(idToken: String): String? {
        return try {
            val credencial = GoogleAuthProvider.getCredential(idToken, null)
            val resultado = auth.signInWithCredential(credencial).await()
            resultado.user?.let { asegurarPerfil(it) }
            null
        } catch (e: Exception) {
            relanzarSiCancelado(e)
            traducirError(e)
        }
    }

    private class GoogleNoConfigurado : Exception("Falta el web client en google-services.json")

    private suspend fun obtenerIdTokenGoogle(context: Context): String {
        // default_web_client_id lo genera el plugin de Google Services SOLO cuando el
        // google-services.json real trae el cliente web (proveedor Google activado).
        // Se busca por nombre para que el proyecto compile tambien con el placeholder.
        val recurso = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (recurso == 0) throw GoogleNoConfigurado()

        val opcion = GetSignInWithGoogleOption.Builder(context.getString(recurso)).build()
        val solicitud = GetCredentialRequest.Builder().addCredentialOption(opcion).build()
        val respuesta = CredentialManager.create(context).getCredential(context, solicitud)

        val credencial = respuesta.credential
        if (credencial is CustomCredential &&
            credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credencial.data).idToken
        }
        throw IllegalStateException("Credencial de Google inesperada")
    }

    // ------------------------------------------------------------------------
    // PERFIL EN FIRESTORE
    // ------------------------------------------------------------------------

    /**
     * Lee usuarios/{uid}. Si el documento no existe o Firestore falla, devuelve un perfil
     * armado con los datos de FirebaseAuth para que la pantalla no se rompa.
     */
    suspend fun perfilActual(): PerfilUsuario? {
        val usuario = usuarioActual ?: return null
        val respaldo = PerfilUsuario(
            uid = usuario.uid,
            nombre = nombreActual(),
            correo = correoActual(),
            rol = ROL_CLIENTE
        )
        return try {
            val doc = FirestorePaths.usuario(usuario.uid).get().await()
            doc.toObject(PerfilUsuario::class.java)?.copy(uid = usuario.uid) ?: respaldo
        } catch (e: Exception) {
            relanzarSiCancelado(e)
            respaldo
        }
    }

    /**
     * Crea usuarios/{uid} con rol "CLIENTE" solo si no existe (asi nunca se pisa
     * el rol de un admin). Si falla no bloquea el inicio de sesion.
     */
    private suspend fun asegurarPerfil(usuario: FirebaseUser, nombreSugerido: String? = null) {
        try {
            val ref = FirestorePaths.usuario(usuario.uid)
            if (!ref.get().await().exists()) {
                val nombre = nombreSugerido?.takeIf { it.isNotBlank() }
                    ?: usuario.displayName
                    ?: usuario.email?.substringBefore("@")
                    ?: ""
                ref.set(
                    PerfilUsuario(
                        uid = usuario.uid,
                        nombre = nombre,
                        correo = usuario.email ?: "",
                        rol = ROL_CLIENTE
                    )
                ).await()
            }
        } catch (e: Exception) {
            relanzarSiCancelado(e)
            Log.w(TAG, "No se pudo crear usuarios/{uid}. Revisa las reglas de Firestore", e)
        }
    }

    // ------------------------------------------------------------------------
    // ERRORES
    // ------------------------------------------------------------------------

    /** Convierte cualquier excepcion de Firebase o de Google en un mensaje en español. */
    fun traducirError(e: Exception): String {
        Log.e(TAG, "Error de autenticacion", e)
        return when (e) {
            is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo"
            // Cubre contraseña incorrecta, credenciales invalidas y correo mal escrito.
            is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
            is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo"
            is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil (mínimo 6 caracteres)"
            is FirebaseAuthException -> when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato válido"
                "ERROR_NETWORK_REQUEST_FAILED" -> "Sin conexión a internet"
                "ERROR_USER_DISABLED" -> "Esta cuenta está deshabilitada"
                else -> "No se pudo completar la operación. Intenta de nuevo"
            }
            is FirebaseNetworkException -> "Sin conexión a internet. Revisa tu red"
            is FirebaseTooManyRequestsException -> "Demasiados intentos. Intenta más tarde"
            is GetCredentialCancellationException -> "Inicio de sesión con Google cancelado"
            is NoCredentialException -> "No hay cuentas de Google disponibles en este dispositivo"
            is GetCredentialException -> "No se pudo iniciar sesión con Google"
            is GoogleNoConfigurado -> "Google aún no está configurado en el proyecto de Firebase"
            else -> "Ocurrió un error inesperado. Intenta de nuevo"
        }
    }

    /** Las corrutinas canceladas deben propagar la cancelacion, no tratarse como error. */
    private fun relanzarSiCancelado(e: Exception) {
        if (e is CancellationException) throw e
    }
}
