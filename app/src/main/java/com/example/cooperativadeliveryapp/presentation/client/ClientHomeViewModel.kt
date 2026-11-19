package com.example.cooperativadeliveryapp.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cooperativadeliveryapp.domain.model.UserProfile
import com.example.cooperativadeliveryapp.domain.repository.CartRepository
import com.example.cooperativadeliveryapp.domain.repository.ClientDiscoveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ClientHomeViewModel @Inject constructor(
    private val clientDiscoveryRepository: ClientDiscoveryRepository,
    cartRepository: CartRepository
) : ViewModel() {

    private val _currentFilter = MutableStateFlow("Todos")
    val currentFilter: StateFlow<String> = _currentFilter.asStateFlow()

    private val allBusinessesFlow = clientDiscoveryRepository.getBusinessesFlow()

    val businesses: StateFlow<List<UserProfile>> = combine(allBusinessesFlow, _currentFilter) { all, filter ->
        if (filter == "Todos") all else all.filter { it.taxonomy?.sector == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableFilters: StateFlow<List<String>> = allBusinessesFlow.map { all ->
        listOf("Todos") + all.mapNotNull { it.taxonomy?.sector }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Todos"))

    val cartFlow = cartRepository.cartFlow

    fun setFilter(filter: String) {
        _currentFilter.update { filter }
    }
}
