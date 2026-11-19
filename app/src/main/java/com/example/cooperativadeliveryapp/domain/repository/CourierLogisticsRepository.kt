package com.example.cooperativadeliveryapp.domain.repository

import com.example.cooperativadeliveryapp.domain.model.CourierOrderOpportunity
import com.example.cooperativadeliveryapp.domain.model.CourierRestBase
import com.example.cooperativadeliveryapp.domain.model.HotspotDemandInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CourierLogisticsRepository {
    val restBasesFlow: StateFlow<List<CourierRestBase>>

    fun switchActiveRestBase(baseId: String)
    fun getAvailableOrdersFlow(): Flow<List<CourierOrderOpportunity>>
    fun getScheduledOrdersFlow(): Flow<List<CourierOrderOpportunity>>
    fun getHotspotDemandFlow(): Flow<List<HotspotDemandInfo>>
    fun acceptOrder(orderId: String): Result<Unit>
    fun suggestOrderToPeer(orderId: String, peerName: String): Result<Unit>
}
