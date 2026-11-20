package mx.cooperativasma.app.domain.repository

import mx.cooperativasma.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserProfile(uid: String): Result<UserProfile?>
    suspend fun saveUserProfile(userProfile: UserProfile): Result<Unit>
    fun getUserProfileFlow(uid: String): Flow<UserProfile?>
    fun getAllUsersFlow(): Flow<List<UserProfile>>
}
