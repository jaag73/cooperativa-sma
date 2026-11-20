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

data class LoginUiState(
    val emailOrPhone: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccessProfile: UserProfile? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var currentPassword = ""

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
        _uiState.update { it.copy(emailOrPhone = email, errorMessage = null) }
    }

    fun login() {
        val emailOrPhone = _uiState.value.emailOrPhone
        val password = currentPassword

        if (emailOrPhone.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor completa ambos campos.") }
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

    fun onLoginHandled() {
        _uiState.update { it.copy(loginSuccessProfile = null) }
    }
}
