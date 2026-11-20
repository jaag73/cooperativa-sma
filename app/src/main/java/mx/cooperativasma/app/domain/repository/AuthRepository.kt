package mx.cooperativasma.app.domain.repository

import mx.cooperativasma.app.domain.model.UserProfile

interface AuthRepository {
    suspend fun login(emailOrPhone: String, password: String): Result<UserProfile>
    suspend fun signInWithPhone(phoneNumber: String): Result<String> // Retorna verificationId
    suspend fun verifyOtp(verificationId: String, code: String): Result<String> // Retorna UID
    suspend fun signInWithGoogle(idToken: String): Result<String> // Retorna UID
    fun getCurrentUserId(): String?
    fun logout()
}
