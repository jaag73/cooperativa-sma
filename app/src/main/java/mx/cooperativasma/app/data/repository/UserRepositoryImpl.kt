package mx.cooperativasma.app.data.repository

import mx.cooperativasma.app.domain.model.*
import mx.cooperativasma.app.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private fun DocumentSnapshot.toUserProfileSafely(): UserProfile? {
        val profile = this.toObject(UserProfile::class.java) ?: return null
        val roleString = this.getString("role") ?: this.getString("userRole") ?: ""
        val email = profile.email
        // Backwards compatibility
        val fallbackRole = when {
            roleString.trim().uppercase() in listOf("BUSINESS", "NEGOCIO") || email.contains("negocio") -> UserRole.BUSINESS.name
            roleString.trim().uppercase() in listOf("COURIER", "REPARTIDOR") || email.contains("repartidor") -> UserRole.COURIER.name
            else -> UserRole.CLIENT.name
        }

        val resolvedRoles = if (profile.roles.isEmpty()) listOf(fallbackRole) else profile.roles
        val resolvedActiveRole = if (profile.activeRole.isEmpty()) resolvedRoles.first() else profile.activeRole

        return profile.copy(roles = resolvedRoles, activeRole = resolvedActiveRole)
    }

    override suspend fun switchActiveRole(uid: String, newRole: String): Result<Unit> {
        return try {
            val docRef = firestore.collection("users").document(uid)
            firestore.runTransaction { transaction ->
                val profile = transaction.get(docRef).toObject(UserProfile::class.java) ?: throw Exception("Profile not found")
                val currentRoles = profile.roles
                if (!currentRoles.contains(newRole)) throw Exception("User doesn't have role $newRole")
                transaction.update(docRef, "activeRole", newRole)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addRoleToProfile(uid: String, newRole: String, extraData: Map<String, Any>?): Result<Unit> {
        return try {
            val docRef = firestore.collection("users").document(uid)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentRoles = snapshot.get("roles") as? List<String> ?: listOf(snapshot.getString("role") ?: "CLIENT")
                
                val updatedRoles = (currentRoles + newRole).distinct()
                val updates = mutableMapOf<String, Any>(
                    "roles" to updatedRoles,
                    "activeRole" to newRole
                )
                extraData?.let { updates.putAll(it) }
                transaction.update(docRef, updates)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfile(uid: String): Result<UserProfile?> {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val profile = doc.toUserProfileSafely()
                if (profile != null) {
                    Result.success(profile)
                } else {
                    Result.failure(Exception("Documento no se pudo convertir a UserProfile"))
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUserProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            firestore.collection("users").document(userProfile.uid).set(userProfile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserProfileFlow(uid: String): Flow<UserProfile?> = callbackFlow {
        val listener = firestore.collection("users").document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val profile = snapshot?.toUserProfileSafely()
            trySend(profile)
        }
        awaitClose { listener.remove() }
    }

    override fun getAllUsersFlow(): Flow<List<UserProfile>> = callbackFlow {
        val listener = firestore.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val profiles = snapshot?.documents?.mapNotNull { it.toUserProfileSafely() } ?: emptyList()
            trySend(profiles)
        }
        awaitClose { listener.remove() }
    }
}
