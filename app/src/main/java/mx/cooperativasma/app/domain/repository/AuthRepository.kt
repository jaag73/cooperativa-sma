package mx.cooperativasma.app.domain.repository

import mx.cooperativasma.app.domain.model.UserProfile
import mx.cooperativasma.app.domain.model.UserRole

interface AuthRepository {
    suspend fun login(emailOrPhone: String, password: String): Result<UserProfile>
    suspend fun registerWithEmail(email: String, password: String, name: String, role: UserRole): Result<UserProfile>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<UserProfile?> // Retorna nulo si el perfil no existe en Firestore
    suspend fun switchActiveRole(newRole: String): Result<Unit>
    suspend fun addRoleToProfile(newRole: String, extraData: Map<String, Any>? = null): Result<Unit>
    fun getCurrentUserId(): String?
    fun logout()
}
