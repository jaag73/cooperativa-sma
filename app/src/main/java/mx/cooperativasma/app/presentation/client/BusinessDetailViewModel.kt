package mx.cooperativasma.app.presentation.client

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import mx.cooperativasma.app.domain.model.*
import mx.cooperativasma.app.domain.repository.CartRepository
import mx.cooperativasma.app.domain.repository.ClientDiscoveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BusinessDetailUiState(
    val isLoading: Boolean = true,
    val business: UserProfile? = null,
    val menu: List<Product> = emptyList(),
    val subcategories: List<String> = listOf("Todos"),
    val currentSubcategory: String = "Todos",
    val error: String? = null
)

@HiltViewModel
class BusinessDetailViewModel @Inject constructor(
    private val clientDiscoveryRepository: ClientDiscoveryRepository,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val businessId: String = checkNotNull(savedStateHandle["businessId"])

    private val _uiState = MutableStateFlow(BusinessDetailUiState())
    val uiState: StateFlow<BusinessDetailUiState> = _uiState.asStateFlow()

    val cartFlow = cartRepository.cartFlow

    init {
        loadBusinessDetails()
    }

    private fun loadBusinessDetails() {
        viewModelScope.launch {
            try {
                val businessList = clientDiscoveryRepository.getBusinessesFlow().firstOrNull()
                val business = businessList?.find { it.uid == businessId }

                clientDiscoveryRepository.getBusinessMenu(businessId).collect { menu ->
                    val subs = listOf("Todos") + menu.map { it.subcategory }.distinct().sorted()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            business = business,
                            menu = menu,
                            subcategories = subs
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setSubcategory(subcat: String) {
        _uiState.update { it.copy(currentSubcategory = subcat) }
    }

    fun addToCart(product: Product, quantity: Int, selectedOptions: List<ProductOptionItem>, specialNotes: String?) {
        val business = _uiState.value.business ?: return
        val cartItem = CartItem(
            cartItemId = System.currentTimeMillis().toString(),
            businessId = business.uid,
            businessName = business.name,
            product = product,
            physicalSpecs = product.physicalSpecs,
            quantity = quantity,
            selectedOptions = selectedOptions,
            specialNotes = specialNotes
        )
        cartRepository.addToCart(cartItem)
    }
}
