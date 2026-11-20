package mx.cooperativasma.app.domain.model

enum class OptionType {
    SINGLE_CHOICE,
    MULTIPLE_CHOICE
}

data class ProductOptionItem(
    val id: String = "",
    val name: String = "",
    val extraPrice: Double = 0.0
)

data class ProductCustomizationGroup(
    val id: String = "",
    val title: String = "",
    val type: OptionType = OptionType.MULTIPLE_CHOICE,
    val isRequired: Boolean = false,
    val minSelections: Int = 0,
    val maxSelections: Int = 1,
    val options: List<ProductOptionItem> = emptyList()
)

data class Product(
    val id: String = "",
    val businessId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "Otros",
    val subcategory: String = "General",
    val isAvailable: Boolean = true,
    val imageUrl: String? = null,
    val customizations: List<ProductCustomizationGroup> = emptyList(),
    val physicalSpecs: ProductPhysicalSpecs = ProductPhysicalSpecs()
)
