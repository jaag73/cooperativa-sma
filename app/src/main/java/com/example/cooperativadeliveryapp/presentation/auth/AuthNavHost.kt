package com.example.cooperativadeliveryapp.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AuthNavHost(
    onAuthSuccess: (com.example.cooperativadeliveryapp.domain.model.UserRole) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is AuthUiState.Success -> {
            LaunchedEffect(state.userProfile.role) {
                onAuthSuccess(state.userProfile.role)
            }
        }
        is AuthUiState.RequireProfileCompletion -> {
            ProfileFormScreen(
                uid = state.uid,
                uiState = state,
                onSubmit = { event ->
                    viewModel.onEvent(event)
                }
            )
        }
        else -> {
            LoginScreen(
                uiState = state,
                onPhoneEntered = { phone ->
                    viewModel.onEvent(AuthUiEvent.OnPhoneEntered(phone))
                },
                onOtpEntered = { otp, verificationId ->
                    viewModel.onEvent(AuthUiEvent.OnOtpCodeEntered(otp, verificationId))
                },
                onNavigateToLogin = onNavigateToLogin
            )
        }
    }
}
