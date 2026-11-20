package mx.cooperativasma.app.data.repository

import mx.cooperativasma.app.domain.model.Product
import mx.cooperativasma.app.domain.model.UserProfile
import mx.cooperativasma.app.domain.model.UserRole
import mx.cooperativasma.app.domain.repository.BusinessMenuRepository
import mx.cooperativasma.app.domain.repository.ClientDiscoveryRepository
import mx.cooperativasma.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientDiscoveryRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val businessMenuRepository: BusinessMenuRepository
) : ClientDiscoveryRepository {

    override fun getBusinessesFlow(): Flow<List<UserProfile>> {
        return userRepository.getAllUsersFlow().map { users ->
            users.filter { it.roles.contains(UserRole.BUSINESS.name) }
        }
    }

    override fun getBusinessMenu(businessId: String): Flow<List<Product>> {
        return businessMenuRepository.getProductsFlow(businessId)
    }
}
