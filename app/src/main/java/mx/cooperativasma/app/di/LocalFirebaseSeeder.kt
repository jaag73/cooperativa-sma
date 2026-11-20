package mx.cooperativasma.app.di

import mx.cooperativasma.app.domain.model.AccountStatus
import mx.cooperativasma.app.domain.model.BusinessBranding
import mx.cooperativasma.app.domain.model.BusinessTaxonomy
import mx.cooperativasma.app.domain.model.CourierRestBase
import mx.cooperativasma.app.domain.model.StructuredAddress
import mx.cooperativasma.app.domain.model.UserProfile
import mx.cooperativasma.app.domain.model.CargoPlacement
import mx.cooperativasma.app.domain.model.FragilityLevel
import mx.cooperativasma.app.domain.model.OptionType
import mx.cooperativasma.app.domain.model.Product
import mx.cooperativasma.app.domain.model.ProductCustomizationGroup
import mx.cooperativasma.app.domain.model.ProductOptionItem
import mx.cooperativasma.app.domain.model.ProductPhysicalSpecs
import mx.cooperativasma.app.domain.model.ThermalRequirement
import mx.cooperativasma.app.domain.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class LocalFirebaseSeeder @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun seedDatabaseIfEmpty() {
        try {
            val usersSnapshot = firestore.collection("users").limit(1).get().await()
            if (usersSnapshot.isEmpty) {
                Log.d("LocalFirebaseSeeder", "Database empty. Seeding...")
                seedUser("cliente@sma.mx", "123456", createClientProfile())
                
                val bizUid = seedUser("negocio@sma.mx", "123456", createBusinessProfile())
                if (bizUid != null) {
                    seedBusinessProducts(bizUid)
                }
                
                seedUser("repartidor@sma.mx", "123456", createCourierProfile())
                seedUser("admin@sma.mx", "123456", createAdminProfile())
                Log.d("LocalFirebaseSeeder", "Seeding complete.")
            }
        } catch (e: Exception) {
            Log.e("LocalFirebaseSeeder", "Error checking/seeding database: ${e.message}", e)
        }
    }

    private suspend fun seedUser(email: String, pass: String, profileCreator: (String, String) -> UserProfile): String? {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: return null
            val profile = profileCreator(uid, email)
            firestore.collection("users").document(uid).set(profile, SetOptions.merge()).await()
            uid
        } catch (e: Exception) {
            Log.e("LocalFirebaseSeeder", "Error seeding user $email", e)
            null
        }
    }

    private suspend fun seedBusinessProducts(businessUid: String) {
        val dummyProducts = listOf(
            Product(
                id = "p1_$businessUid",
                businessId = businessUid,
                name = "Torta de Milanesa",
                description = "Deliciosa torta con milanesa de cerdo",
                price = 65.0,
                category = "Platos Fuertes",
                subcategory = "Con carne",
                physicalSpecs = ProductPhysicalSpecs(volumeUnits = 2, thermalRequirement = ThermalRequirement.HOT, fragility = FragilityLevel.MODERATE, cargoPlacement = CargoPlacement.THERMAL_BACKPACK),
                customizations = listOf(
                    ProductCustomizationGroup(
                        id = "g1",
                        title = "Preparación",
                        type = OptionType.MULTIPLE_CHOICE,
                        options = listOf(
                            ProductOptionItem(id = "o1", name = "Sin cebolla", extraPrice = 0.0),
                            ProductOptionItem(id = "o2", name = "Sin picante", extraPrice = 0.0)
                        )
                    ),
                    ProductCustomizationGroup(
                        id = "g2",
                        title = "Extras",
                        type = OptionType.MULTIPLE_CHOICE,
                        options = listOf(
                            ProductOptionItem(id = "o3", name = "Queso Oaxaca extra", extraPrice = 15.0),
                            ProductOptionItem(id = "o4", name = "Aguacate extra", extraPrice = 10.0)
                        )
                    )
                )
            ),
            Product(
                id = "p2_$businessUid",
                businessId = businessUid,
                name = "Pastel Imposible",
                description = "Rebanada individual",
                price = 45.0,
                category = "Postres",
                subcategory = "Postres",
                physicalSpecs = ProductPhysicalSpecs(volumeUnits = 1, thermalRequirement = ThermalRequirement.COLD, fragility = FragilityLevel.DELICATE, cargoPlacement = CargoPlacement.THERMAL_BACKPACK)
            )
        )
        
        for (product in dummyProducts) {
            firestore.collection("products").document(product.id).set(product).await()
        }
    }

    private fun createClientProfile(): (String, String) -> UserProfile = { uid, email ->
        UserProfile(
            uid = uid,
            email = email,
            name = "Cliente Demo",
            roles = listOf(UserRole.CLIENT.name),
            activeRole = UserRole.CLIENT.name,
            status = AccountStatus.ACTIVE
        )
    }

    private fun createBusinessProfile(): (String, String) -> UserProfile = { uid, email ->
        UserProfile(
            uid = uid,
            email = email,
            name = "Tortas El Sazón SMA",
            roles = listOf(UserRole.BUSINESS.name),
            activeRole = UserRole.BUSINESS.name,
            status = AccountStatus.ACTIVE,
            taxonomy = BusinessTaxonomy(
                sector = "Restaurante / Comida",
                category = "Tortas"
            ),
            structuredAddress = StructuredAddress(
                city = "San Miguel de Allende",
                postalCode = "37700",
                neighborhoodOrCommunity = "Centro",
                street = "Mesones",
                exteriorNumber = "45"
            ),
            branding = BusinessBranding(
                slogan = "Las mejores tortas del centro",
                logoUrl = "", 
                facadePhotoUrl = ""
            )
        )
    }

    private fun createCourierProfile(): (String, String) -> UserProfile = { uid, email ->
        UserProfile(
            uid = uid,
            email = email,
            name = "Repartidor Demo",
            roles = listOf(UserRole.COURIER.name),
            activeRole = UserRole.COURIER.name,
            status = AccountStatus.ACTIVE,
            primaryRestBase = CourierRestBase(
                id = "base_1",
                label = "Mi Casa",
                neighborhood = "Santas Marías",
                postalCode = "37712",
                isCurrentActiveBase = true
            ),
            secondaryRestBase = CourierRestBase(
                id = "base_2",
                label = "Casa Familiar",
                neighborhood = "San Antonio",
                postalCode = "37710",
                isCurrentActiveBase = false
            )
        )
    }

    private fun createAdminProfile(): (String, String) -> UserProfile = { uid, email ->
        UserProfile(
            uid = uid,
            email = email,
            name = "Super Admin SMA",
            roles = listOf(UserRole.CLIENT.name, UserRole.BUSINESS.name, UserRole.COURIER.name),
            activeRole = UserRole.CLIENT.name,
            status = AccountStatus.ACTIVE,
            taxonomy = BusinessTaxonomy(
                sector = "Restaurante / Comida",
                category = "Tortas"
            ),
            structuredAddress = StructuredAddress(
                city = "San Miguel de Allende",
                postalCode = "37700",
                neighborhoodOrCommunity = "Centro",
                street = "Mesones",
                exteriorNumber = "45"
            ),
            primaryRestBase = CourierRestBase(
                id = "base_1",
                label = "Mi Casa",
                neighborhood = "Santas Marías",
                postalCode = "37712",
                isCurrentActiveBase = true
            )
        )
    }
}