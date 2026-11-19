package com.example.cooperativadeliveryapp.presentation.business

import com.example.cooperativadeliveryapp.domain.model.Product
import com.example.cooperativadeliveryapp.domain.model.UserProfile

sealed interface BusinessUiState {
    data object Loading : BusinessUiState
    data class Success(
        val userProfile: UserProfile,
        val orders: List<com.example.cooperativadeliveryapp.domain.model.LiveOrder>,
        val menu: List<Product>,
        val currentTab: DashboardTab = DashboardTab.LIVE_ORDERS,
        val currentOrderFilter: OrderFilter = OrderFilter.NEW,
        val currentMenuSubcategory: String = "Todos"
    ) : BusinessUiState
    data class Error(val message: String) : BusinessUiState
}

enum class DashboardTab {
    LIVE_ORDERS, MENU, FINANCES
}

enum class OrderFilter {
    NEW, PREPARING, READY, HISTORY
}

sealed interface BusinessUiEvent {
    data class OnTabSelected(val tab: DashboardTab) : BusinessUiEvent
    data class OnOrderFilterSelected(val filter: OrderFilter) : BusinessUiEvent
    data class OnMenuSubcategorySelected(val subcategory: String) : BusinessUiEvent
    data class OnAcceptOrder(val orderId: String, val prepTimeMinutes: Int) : BusinessUiEvent
    data class OnRejectOrder(val orderId: String) : BusinessUiEvent
    data class OnMarkOrderReady(val orderId: String) : BusinessUiEvent
    data class OnToggleProductAvailability(val productId: String, val isAvailable: Boolean) : BusinessUiEvent
    data class OnNavigateToEditProduct(val productId: String?) : BusinessUiEvent
    data object OnStopAlert : BusinessUiEvent
}
