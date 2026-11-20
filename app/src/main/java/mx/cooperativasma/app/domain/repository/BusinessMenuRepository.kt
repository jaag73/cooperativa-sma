package mx.cooperativasma.app.domain.repository

import mx.cooperativasma.app.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface BusinessMenuRepository {
    fun getProductsFlow(businessId: String): Flow<List<Product>>
    suspend fun saveProduct(product: Product): Result<Unit>
    suspend fun toggleProductAvailability(productId: String, isAvailable: Boolean): Result<Unit>
}
