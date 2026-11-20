package mx.cooperativasma.app.presentation.courier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import mx.cooperativasma.app.domain.model.CourierOrderOpportunity
import mx.cooperativasma.app.domain.model.CourierRestBase
import mx.cooperativasma.app.domain.model.HotspotDemandInfo
import mx.cooperativasma.app.domain.repository.AuthRepository
import mx.cooperativasma.app.domain.repository.CourierLogisticsRepository
import mx.cooperativasma.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourierDashboardUiState(
    val currentTab: CourierTab = CourierTab.LIVE_RADAR,
    val restBases: List<CourierRestBase> = emptyList(),
    val activeBase: CourierRestBase? = null,
    val availableOrders: List<CourierOrderOpportunity> = emptyList(),
    val scheduledOrders: List<CourierOrderOpportunity> = emptyList(),
    val hotspots: List<HotspotDemandInfo> = emptyList(),
    val todayNetEarnings: Double = 230.0 // Simulando progreso del día
)

enum class CourierTab {
    LIVE_RADAR, SCHEDULED, ZONES_FIFO
}

@HiltViewModel
class CourierDashboardViewModel @Inject constructor(
    private val logisticsRepository: CourierLogisticsRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentTab = MutableStateFlow(CourierTab.LIVE_RADAR)
    private val _userBases = MutableStateFlow<List<CourierRestBase>>(emptyList())
    
    init {
        loadUserBases()
    }

    private fun loadUserBases() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            val profileResult = userRepository.getUserProfile(uid)
            profileResult.onSuccess { profile ->
                val bases = mutableListOf<CourierRestBase>()
                profile?.primaryRestBase?.let { bases.add(it.copy(isCurrentActiveBase = true)) }
                profile?.secondaryRestBase?.let { bases.add(it.copy(isCurrentActiveBase = false)) }
                
                // Fallback seguro si viene nulo
                if (bases.isEmpty()) {
                    bases.add(CourierRestBase(id = "fallback", label = "Base Predefinida", neighborhood = "Santas Marías", postalCode = "37712", isCurrentActiveBase = true))
                }
                
                _userBases.value = bases
            }
        }
    }

    val uiState: StateFlow<CourierDashboardUiState> = combine(
        _currentTab,
        _userBases,
        logisticsRepository.getAvailableOrdersFlow(),
        logisticsRepository.getScheduledOrdersFlow(),
        logisticsRepository.getHotspotDemandFlow()
    ) { tab, bases, available, scheduled, hotspots ->
        CourierDashboardUiState(
            currentTab = tab,
            restBases = bases,
            activeBase = bases.find { it.isCurrentActiveBase } ?: bases.firstOrNull(),
            availableOrders = available,
            scheduledOrders = scheduled,
            hotspots = hotspots
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CourierDashboardUiState())

    fun selectTab(tab: CourierTab) {
        _currentTab.value = tab
    }

    fun switchActiveBase(baseId: String) {
        val updated = _userBases.value.map { it.copy(isCurrentActiveBase = (it.id == baseId)) }
        _userBases.value = updated
    }
}
