package mx.cooperativasma.app.di

import mx.cooperativasma.app.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        val auth = FirebaseAuth.getInstance()
        if (BuildConfig.DEBUG) {
            try {
                auth.useEmulator("127.0.0.1", 9099)
            } catch (_: Exception) {
                // Ignore if already configured
            }
        }
        return auth
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        if (BuildConfig.DEBUG) {
            try {
                firestore.useEmulator("127.0.0.1", 8080)
                firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(com.google.firebase.firestore.MemoryCacheSettings.newBuilder().build())
                    .build()
            } catch (_: Exception) {
                // Ignore if already configured
            }
        }
        return firestore
    }
}
