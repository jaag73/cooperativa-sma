package com.example.cooperativadeliveryapp.presentation.auth

import com.example.cooperativadeliveryapp.domain.model.MotorcycleSpecs
import com.example.cooperativadeliveryapp.domain.model.UserProfile
import com.example.cooperativadeliveryapp.domain.model.UserRole

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
        val taxonomy: com.example.cooperativadeliveryapp.domain.model.BusinessTaxonomy? = null,
        val structuredAddress: com.example.cooperativadeliveryapp.domain.model.StructuredAddress? = null,
        val identityVerification: com.example.cooperativadeliveryapp.domain.model.BusinessIdentityVerification? = null,
        val branding: com.example.cooperativadeliveryapp.domain.model.BusinessBranding? = null,
        val motorcycleSpecs: MotorcycleSpecs? = null,
        val courierLicenseUrl: String? = null,
        val circulationCardUrl: String? = null,
        val primaryRestBase: com.example.cooperativadeliveryapp.domain.model.CourierRestBase? = null,
        val secondaryRestBase: com.example.cooperativadeliveryapp.domain.model.CourierRestBase? = null,
        val rfc: String? = null,
        val clabe: String? = null
    ) : AuthUiEvent
}
