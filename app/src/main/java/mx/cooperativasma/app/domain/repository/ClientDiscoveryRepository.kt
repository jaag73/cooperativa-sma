package mx.cooperativasma.app.domain.repository

import mx.cooperativasma.app.domain.model.Product
import mx.cooperativasma.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ClientDiscoveryRepository {
    fun getBusinessesFlow(): Flow<List<UserProfile>>
    fun getBusinessMenu(businessId: String): Flow<List<Product>>
}
