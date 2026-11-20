package mx.cooperativasma.app.data.repository

import mx.cooperativasma.app.domain.model.UserProfile
import mx.cooperativasma.app.domain.repository.AuthRepository
import mx.cooperativasma.app.domain.repository.UserRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(emailOrPhone: String, password: String): Result<UserProfile> {
        return try {
            val email = emailOrPhone.trim()
            val pass = password.trim()
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("UID not found")
            
            var profileResult = userRepository.getUserProfile(uid)
            if (profileResult.isSuccess && profileResult.getOrNull() != null) {
                Result.success(profileResult.getOrNull()!!)
            } else {
                // Fallback: search by email
                val snapshot = firestore.collection("users").whereEqualTo("email", email).get().await()
                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents.first()
                    var profile = doc.toObject(UserProfile::class.java)
                    if (profile != null) {
                        val roleString = doc.getString("role") ?: doc.getString("userRole") ?: ""
                        val resolvedRole = when {
                            roleString.trim().uppercase() in listOf("BUSINESS", "NEGOCIO") || email.contains("negocio") -> mx.cooperativasma.app.domain.model.UserRole.BUSINESS
                            roleString.trim().uppercase() in listOf("COURIER", "REPARTIDOR") || email.contains("repartidor") -> mx.cooperativasma.app.domain.model.UserRole.COURIER
                            else -> mx.cooperativasma.app.domain.model.UserRole.CLIENT
                        }
                        profile = profile.copy(role = resolvedRole)
                        Result.success(profile)
                    } else {
                        Result.failure(Exception("Usuario autenticado en Auth, pero sin perfil válido en Firestore para el UID: $uid"))
                    }
                } else {
                    Result.failure(Exception("Usuario autenticado en Auth, pero sin perfil en Firestore para el UID: $uid"))
                }
            }
        } catch (e: FirebaseNetworkException) {
            Result.failure(Exception("Error de red: No se pudo conectar al emulador local (10.0.2.2:9099/8080)"))
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("Correo o contraseña incorrectos"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Correo o contraseña incorrectos"))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Error desconocido al iniciar sesión"))
        }
    }

    override suspend fun signInWithPhone(phoneNumber: String): Result<String> {
        return Result.success("dummy_verification_id")
    }

    override suspend fun verifyOtp(verificationId: String, code: String): Result<String> {
        return Result.success("dummy_uid")
    }

    override suspend fun signInWithGoogle(idToken: String): Result<String> {
        return Result.success("dummy_uid")
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override fun logout() {
        firebaseAuth.signOut()
    }
}
