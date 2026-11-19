package com.example.cooperativadeliveryapp.presentation.business

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cooperativadeliveryapp.domain.model.Product
import com.example.cooperativadeliveryapp.domain.repository.AuthRepository
import com.example.cooperativadeliveryapp.domain.repository.BusinessMenuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class AddEditProductViewModel @Inject constructor(
    private val menuRepository: BusinessMenuRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveComplete = MutableStateFlow(false)
    val saveComplete: StateFlow<Boolean> = _saveComplete.asStateFlow()

    init {
        savedStateHandle.get<String>("productId")?.let { productId ->
            if (productId.isNotBlank()) {
                loadProduct(productId)
            }
        }
    }

    private fun loadProduct(productId: String) {
        val businessId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val products = menuRepository.getProductsFlow(businessId).firstOrNull()
            val productToEdit = products?.find { it.id == productId }
            _product.value = productToEdit
        }
    }

    fun saveProduct(product: Product) {
        val businessId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _isSaving.value = true
            val productToSave = product.copy(businessId = businessId)
            Log.d("SMA_CATALOG", "GUARDANDO PRODUCTO -> name=${productToSave.name}, businessId=${productToSave.businessId}")
            menuRepository.saveProduct(productToSave)
            _isSaving.value = false
            _saveComplete.value = true
        }
    }
}
