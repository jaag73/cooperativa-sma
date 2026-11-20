package mx.cooperativasma.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import mx.cooperativasma.app.domain.model.AccountStatus
import mx.cooperativasma.app.domain.model.MembershipTrial
import mx.cooperativasma.app.domain.model.UserProfile
import mx.cooperativasma.app.domain.model.UserRole
import mx.cooperativasma.app.domain.repository.AuthRepository
import mx.cooperativasma.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.OnCompleteProfileSubmit -> handleProfileComplete(event)
            else -> {}
        }
    }

    private suspend fun checkUserProfile(uid: String) {
        userRepository.getUserProfile(uid).fold(
            onSuccess = { profile ->
                if (profile != null) {
                    _uiState.update { AuthUiState.Success(profile) }
                } else {
                    _uiState.update { AuthUiState.RequireProfileCompletion(uid, null) }
                }
            },
            onFailure = { error ->
                _uiState.update { AuthUiState.Error(error.message ?: "Error al obtener el perfil") }
            }
        )
    }

    private fun handleProfileComplete(event: AuthUiEvent.OnCompleteProfileSubmit) {
        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading }

            val isBusinessOrCourier = event.role == UserRole.BUSINESS || event.role == UserRole.COURIER

            val trialInfo = if (isBusinessOrCourier) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                val startDate = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_YEAR, 30)
                val endDate = dateFormat.format(calendar.time)
                MembershipTrial(
                    trialStartDate = startDate,
                    trialEndDate = endDate,
                    isTrialActive = true
                )
            } else null

            val status = if (event.role == UserRole.COURIER) AccountStatus.PENDING_VERIFICATION else AccountStatus.ACTIVE

            val profile = UserProfile(
                uid = if (event.uid.isBlank()) "dummy_uid" else event.uid,
                name = event.name,
                roles = listOf(event.role.name),
                activeRole = event.role.name,
                communityId = event.communityId,
                status = status,
                trialInfo = trialInfo,
                cashPaymentAllowed = false,
                taxonomy = event.taxonomy,
                structuredAddress = event.structuredAddress,
                identityVerification = event.identityVerification,
                branding = event.branding,
                motorcycle = event.motorcycleSpecs,
                courierLicenseUrl = event.courierLicenseUrl,
                circulationCardUrl = event.circulationCardUrl,
                primaryRestBase = event.primaryRestBase,
                secondaryRestBase = event.secondaryRestBase,
                rfc = event.rfc,
                clabe = event.clabe
            )

            userRepository.saveUserProfile(profile).fold(
                onSuccess = {
                    _uiState.update { AuthUiState.Success(profile) }
                },
                onFailure = { error ->
                    _uiState.update { AuthUiState.Error(error.message ?: "Error al guardar el perfil") }
                }
            )
        }
    }
}
