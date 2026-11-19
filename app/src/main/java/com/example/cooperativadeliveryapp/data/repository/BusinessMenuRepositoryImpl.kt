package com.example.cooperativadeliveryapp.data.repository

import com.example.cooperativadeliveryapp.domain.model.*
import com.example.cooperativadeliveryapp.domain.repository.BusinessMenuRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class BusinessMenuRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BusinessMenuRepository {

    private fun DocumentSnapshot.toProductSafely(): Product? {
        val id = getString("id") ?: id
        val businessId = getString("businessId") ?: ""
        val name = getString("name") ?: ""
        val description = getString("description") ?: ""
        val price = getDouble("price") ?: 0.0
        val category = getString("category") ?: ""
        val subcategory = getString("subcategory") ?: ""
        val isAvailable = getBoolean("isAvailable") ?: getBoolean("available") ?: true
        val imageUrl = getString("imageUrl")

        val physicalSpecsMap = get("physicalSpecs") as? Map<String, Any>
        val prepTimeMinutes = (physicalSpecsMap?.get("prepTimeMinutes") as? Number)?.toInt() ?: 10
        val volumeUnits = (physicalSpecsMap?.get("volumeUnits") as? Number)?.toInt() ?: 1
        val isFragile = (physicalSpecsMap?.get("isFragile") as? Boolean) ?: false
        
        val thermalReqStr = physicalSpecsMap?.get("thermalRequirement") as? String ?: ""
        val thermalRequirement = when (thermalReqStr.trim().uppercase()) {
            "HOT" -> ThermalRequirement.HOT
            "COLD" -> ThermalRequirement.COLD
            else -> ThermalRequirement.AMBIENT
        }
        
        val fragStr = physicalSpecsMap?.get("fragility") as? String ?: ""
        val fragility = when (fragStr.trim().uppercase()) {
            "DELICATE" -> FragilityLevel.DELICATE
            else -> FragilityLevel.ROBUST
        }
        
        val cargoStr = physicalSpecsMap?.get("cargoPlacement") as? String ?: ""
        val cargoPlacement = when (cargoStr.trim().uppercase()) {
            "REAR_RACK_STRAPS" -> CargoPlacement.REAR_RACK_STRAPS
            else -> CargoPlacement.THERMAL_BACKPACK
        }

        val baseObj = try { toObject(Product::class.java) } catch (_: Exception) { null }

        return Product(
            id = id,
            businessId = businessId,
            name = name,
            description = description,
            price = price,
            category = category,
            subcategory = subcategory,
            isAvailable = isAvailable,
            imageUrl = imageUrl,
            customizations = baseObj?.customizations ?: emptyList(),
            physicalSpecs = ProductPhysicalSpecs(
                prepTimeMinutes = prepTimeMinutes,
                volumeUnits = volumeUnits,
                isFragile = isFragile,
                thermalRequirement = thermalRequirement,
                fragility = fragility,
                cargoPlacement = cargoPlacement
            )
        )
    }

    override fun getProductsFlow(businessId: String): Flow<List<Product>> = callbackFlow {
        val listener = firestore.collection("products")
            .whereEqualTo("businessId", businessId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val products = snapshot?.documents?.mapNotNull { it.toProductSafely() } ?: emptyList()
                Log.d("SMA_CATALOG", "Cliente/Negocio recibió ${products.size} productos desde Firestore para businessId: $businessId")
                trySend(products)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveProduct(product: Product): Result<Unit> {
        return try {
            val docRef = if (product.id.isBlank()) {
                firestore.collection("products").document()
            } else {
                firestore.collection("products").document(product.id)
            }
            val finalProduct = product.copy(id = docRef.id)
            docRef.set(finalProduct).await()
            Log.d("SMA_CATALOG", "Producto guardado en Firestore: id=${finalProduct.id}, businessId=${finalProduct.businessId}, name=${finalProduct.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleProductAvailability(productId: String, isAvailable: Boolean): Result<Unit> {
        return try {
            firestore.collection("products").document(productId)
                .update("isAvailable", isAvailable).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
