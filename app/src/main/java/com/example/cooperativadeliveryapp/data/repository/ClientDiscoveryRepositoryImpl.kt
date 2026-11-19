package com.example.cooperativadeliveryapp.data.repository

import com.example.cooperativadeliveryapp.domain.model.Product
import com.example.cooperativadeliveryapp.domain.model.UserProfile
import com.example.cooperativadeliveryapp.domain.model.UserRole
import com.example.cooperativadeliveryapp.domain.repository.BusinessMenuRepository
import com.example.cooperativadeliveryapp.domain.repository.ClientDiscoveryRepository
import com.example.cooperativadeliveryapp.domain.repository.UserRepository
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
            users.filter { it.role == UserRole.BUSINESS }
        }
    }

    override fun getBusinessMenu(businessId: String): Flow<List<Product>> {
        return businessMenuRepository.getProductsFlow(businessId)
    }
}
