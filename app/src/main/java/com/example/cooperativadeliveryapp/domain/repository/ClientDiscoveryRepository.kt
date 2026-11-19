package com.example.cooperativadeliveryapp.domain.repository

import com.example.cooperativadeliveryapp.domain.model.Product
import com.example.cooperativadeliveryapp.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ClientDiscoveryRepository {
    fun getBusinessesFlow(): Flow<List<UserProfile>>
    fun getBusinessMenu(businessId: String): Flow<List<Product>>
}
