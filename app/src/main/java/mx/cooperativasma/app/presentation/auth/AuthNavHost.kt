package mx.cooperativasma.app.presentation.auth

import mx.cooperativasma.app.domain.model.UserRole
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AuthNavHost(
    onAuthSuccess: (mx.cooperativasma.app.domain.model.UserRole) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is AuthUiState.Success -> {
            LaunchedEffect(state.userProfile.activeRole) {
                onAuthSuccess(UserRole.valueOf(state.userProfile.activeRole))
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
            LaunchedEffect(Unit) {
                onNavigateToLogin()
            }
        }
    }
}
