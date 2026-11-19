package com.example.cooperativadeliveryapp.data.repository

import com.example.cooperativadeliveryapp.domain.model.*
import com.example.cooperativadeliveryapp.domain.repository.UserRepository
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
        val resolvedRole = when {
            roleString.trim().uppercase() in listOf("BUSINESS", "NEGOCIO") || email.contains("negocio") -> UserRole.BUSINESS
            roleString.trim().uppercase() in listOf("COURIER", "REPARTIDOR") || email.contains("repartidor") -> UserRole.COURIER
            roleString.trim().uppercase() in listOf("CLIENT", "CLIENTE") || email.contains("cliente") -> UserRole.CLIENT
            else -> profile.role
        }
        return profile.copy(role = resolvedRole)
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
