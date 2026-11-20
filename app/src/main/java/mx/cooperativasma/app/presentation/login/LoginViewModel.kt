package mx.cooperativasma.app.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import mx.cooperativasma.app.domain.model.UserProfile
import mx.cooperativasma.app.domain.model.UserRole
import mx.cooperativasma.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import android.content.Context
import mx.cooperativasma.app.R

import mx.cooperativasma.app.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import mx.cooperativasma.app.domain.model.AccountStatus

data class LoginUiState(
    val emailOrPhone: String = "",
    val name: String = "",
    val selectedRole: UserRole = UserRole.CLIENT,
    val isRegisterMode: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccessProfile: UserProfile? = null,
    val showGoogleOnboardingDialog: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var currentPassword = ""

    fun toggleRegisterMode() {
        _uiState.update { it.copy(isRegisterMode = !it.isRegisterMode, errorMessage = null) }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }

    fun onRoleSelect(role: UserRole) {
        _uiState.update { it.copy(selectedRole = role, errorMessage = null) }
    }

    fun onEmailOrPhoneChange(value: String) {
        _uiState.update { it.copy(emailOrPhone = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        currentPassword = value
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun getPassword() = currentPassword

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun fillDemoCredentials(role: UserRole) {
        val email = when (role) {
            UserRole.CLIENT -> "cliente@sma.mx"
            UserRole.BUSINESS -> "negocio@sma.mx"
            UserRole.COURIER -> "repartidor@sma.mx"
        }
        currentPassword = "123456"
        _uiState.update { it.copy(emailOrPhone = email, isRegisterMode = false, errorMessage = null) }
    }

    fun login() {
        val emailOrPhone = _uiState.value.emailOrPhone.trim()
        val password = currentPassword.trim()

        if (emailOrPhone.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor completa todos los campos.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.login(emailOrPhone, password).fold(
                onSuccess = { profile ->
                    _uiState.update { it.copy(isLoading = false, loginSuccessProfile = profile) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun register() {
        val email = _uiState.value.emailOrPhone.trim()
        val password = currentPassword.trim()
        val name = _uiState.value.name.trim()
        val role = _uiState.value.selectedRole

        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor completa todos los campos para el registro.") }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.registerWithEmail(email, password, name, role).fold(
                onSuccess = { profile ->
                    _uiState.update { it.copy(isLoading = false, loginSuccessProfile = profile) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Error al registrar cuenta.") }
                }
            )
        }
    }

    fun sendPasswordResetEmail() {
        val email = _uiState.value.emailOrPhone.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa tu correo en el campo correspondiente para restablecer la contraseña.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.sendPasswordResetEmail(email).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Correo de recuperación enviado a $email") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Error al enviar correo de recuperación.") }
                }
            )
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(true)
                    .build()
                    
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                    
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                
                if (credential is GoogleIdTokenCredential) {
                    val idToken = credential.idToken
                    authRepository.signInWithGoogle(idToken).fold(
                        onSuccess = { profile ->
                            if (profile != null) {
                                _uiState.update { it.copy(isLoading = false, loginSuccessProfile = profile) }
                            } else {
                                // User authenticated with Google but lacks a Firestore UserProfile -> Trigger Onboarding Dialog
                                _uiState.update { it.copy(isLoading = false, showGoogleOnboardingDialog = true) }
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                        }
                    )
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error: Credencial inválida") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error al iniciar sesión con Google") }
            }
        }
    }

    fun completeGoogleOnboarding(selectedRole: UserRole) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                val profile = UserProfile(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName?.ifBlank { "Usuario Cooperativa" } ?: "Usuario Cooperativa",
                    roles = listOf(selectedRole.name),
                    activeRole = selectedRole.name,
                    status = AccountStatus.ACTIVE
                )
                userRepository.saveUserProfile(profile).fold(
                    onSuccess = {
                        _uiState.update { it.copy(isLoading = false, showGoogleOnboardingDialog = false, loginSuccessProfile = profile) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isLoading = false, showGoogleOnboardingDialog = false, errorMessage = error.message ?: "Error al crear perfil.") }
                    }
                )
            } else {
                _uiState.update { it.copy(isLoading = false, showGoogleOnboardingDialog = false, errorMessage = "Error: Sesión de Google expirada.") }
            }
        }
    }

    fun dismissGoogleOnboardingDialog() {
        _uiState.update { it.copy(showGoogleOnboardingDialog = false) }
    }

    fun onLoginHandled() {
        _uiState.update { it.copy(loginSuccessProfile = null) }
    }
}
