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
 * Gestiona el registro y el inicio de sesión de los usuarios
 * mediante correo electrónico o cuenta de Google.
 *
 * También se encarga de mantener el perfil del usuario en Firestore.
 */
object AuthRepository {

    private const val TAG = "AuthRepository"
    private const val ROL_CLIENTE = "CLIENTE"

    private val auth get() = FirebaseAuth.getInstance()

    /** Obtiene el usuario que tiene la sesión iniciada, si existe. */
    private val usuarioActual: FirebaseUser?
        get() = try {
            FirebaseAuth.getInstance().currentUser
        } catch (e: Exception) {
            null
        }

    fun uidActual(): String? = usuarioActual?.uid

    /** Nombre para mostrar en pantalla (Home, Perfil). "Invitado" si no hay sesion. */
    fun nombreActual(): String = usuarioActual?.let { usuario ->
        usuario.displayName?.takeIf { it.isNotBlank() } ?: usuario.email?.substringBefore("@")
    } ?: "Invitado"

    fun correoActual(): String = usuarioActual?.email ?: ""


    fun cerrarSesion() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo cerrar sesion", e)
        }
    }

    /** Registra una cuenta nueva y guarda sus datos básicos en Firebase. */
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

    suspend fun recuperarPassword(correo: String): String? {
        return try {
            auth.sendPasswordResetEmail(correo).await()
            null
        } catch (e: Exception) {
            relanzarSiCancelado(e)
            traducirError(e)
        }
    }


    /**
     * Permite iniciar sesión con una cuenta de Google.
     * Obtiene las credenciales seleccionadas y las utiliza para autenticar al usuario en Firebase.
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

    /**
     * Inicia sesión en Firebase utilizando las credenciales obtenidas de Google.
     * Si es un usuario nuevo, crea su perfil en Firestore.
     */
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
        // Busca el identificador del cliente web que Firebase utiliza para iniciar sesión con Google.
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

    /**
     * Obtiene los datos del perfil desde Firestore.
     * Si no están disponibles, utiliza la información de la cuenta de Firebase
     * para que la aplicación pueda seguir funcionando.
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
     * Crea el perfil del usuario si todavía no existe.
     * Si ya está registrado, conserva sus datos y su rol actual.
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

    /** Convierte los errores de Firebase y Google en mensajes fáciles de entender. */
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

    /** Permite que las cancelaciones de corrutinas se propaguen correctamente. */
    private fun relanzarSiCancelado(e: Exception) {
        if (e is CancellationException) throw e
    }
}
