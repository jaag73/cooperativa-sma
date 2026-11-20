package mx.cooperativasma.app.presentation.auth

import mx.cooperativasma.app.domain.model.MotorcycleSpecs
import mx.cooperativasma.app.domain.model.UserProfile
import mx.cooperativasma.app.domain.model.UserRole

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class RequireProfileCompletion(val uid: String, val phone: String?) : AuthUiState
    data class Success(val userProfile: UserProfile) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

sealed interface AuthUiEvent {
    data class OnPhoneEntered(val phoneNumber: String) : AuthUiEvent
    data class OnOtpCodeEntered(val code: String, val verificationId: String) : AuthUiEvent
    data class OnCompleteProfileSubmit(
        val uid: String,
        val role: UserRole,
        val name: String,
        val communityId: String?,
        val managerName: String? = null,
        val address: String? = null,
        val taxonomy: mx.cooperativasma.app.domain.model.BusinessTaxonomy? = null,
        val structuredAddress: mx.cooperativasma.app.domain.model.StructuredAddress? = null,
        val identityVerification: mx.cooperativasma.app.domain.model.BusinessIdentityVerification? = null,
        val branding: mx.cooperativasma.app.domain.model.BusinessBranding? = null,
        val motorcycleSpecs: MotorcycleSpecs? = null,
        val courierLicenseUrl: String? = null,
        val circulationCardUrl: String? = null,
        val primaryRestBase: mx.cooperativasma.app.domain.model.CourierRestBase? = null,
        val secondaryRestBase: mx.cooperativasma.app.domain.model.CourierRestBase? = null,
        val rfc: String? = null,
        val clabe: String? = null
    ) : AuthUiEvent
}
