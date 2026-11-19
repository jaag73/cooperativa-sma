package com.example.cooperativadeliveryapp.domain.model

enum class ThermalRequirement(val label: String) {
    AMBIENT("Temperatura Ambiente / Seco"),
    HOT("🔥 Requiere llegar caliente"),
    COLD("❄️ Producto frío / congelado")
}

enum class CargoPlacement {
    THERMAL_BACKPACK, // Mochila térmica del repartidor
    REAR_RACK_STRAPS  // Parrilla con ligas elásticas de sujeción
}

enum class FragilityLevel {
    ROBUST,      // Latas, bolsas secas, tortas compactas
    MODERATE,    // Charolas plásticas, fruta suave
    DELICATE     // Huevos, pasteles, frascos de vidrio, postres delicados
}

enum class ProductVolumeSize(val label: String, val capacityUnits: Int) {
    SMALL("Chico (Refresco / Taco)", 1),
    MEDIUM("Mediano (Torta / Combo)", 2),
    LARGE("Grande (Charola / Bolsa despensa)", 4),
    BULKY("Voluminoso (Garrafón / Caja grande)", 6)
}

enum class ProductWeightCategory(val label: String, val requiresRack: Boolean) {
    LIGHT("Ligero (Mochila)", false),
    MEDIUM("Peso Medio (Mochila / Parrilla)", false),
    HEAVY("Pesado (Parrilla con ligas obligatoria)", true)
}

data class ProductPhysicalSpecs(
    val volumeUnits: Int = 1,
    val thermalRequirement: ThermalRequirement = ThermalRequirement.HOT,
    val fragility: FragilityLevel = FragilityLevel.ROBUST,
    val cargoPlacement: CargoPlacement = CargoPlacement.THERMAL_BACKPACK,
    val prepTimeMinutes: Int = 10,
    val isFragile: Boolean = false,
    val volumeSize: ProductVolumeSize = ProductVolumeSize.MEDIUM,
    val weightCategory: ProductWeightCategory = ProductWeightCategory.LIGHT
)

data class CartItem(
    val cartItemId: String = "",
    val businessId: String = "",
    val businessName: String = "",
    val product: Product = Product(),
    val physicalSpecs: ProductPhysicalSpecs = ProductPhysicalSpecs(),
    val quantity: Int = 1,
    val selectedOptions: List<ProductOptionItem> = emptyList(),
    val specialNotes: String? = null
) {
    val unitPriceWithCustomizations: Double
        get() = product.price + selectedOptions.sumOf { it.extraPrice }

    val totalPrice: Double
        get() = unitPriceWithCustomizations * quantity
}

data class MultiBusinessCart(
    val items: List<CartItem> = emptyList(),
    val maxBackpackUnits: Int = 8,
    val maxRackUnits: Int = 6,
    val maxDelicateItems: Int = 2,
    val mockDeliveryFee: Double = 45.0 // Tarifa ficticia base multinegocio
) {
    val subtotal: Double
        get() = items.sumOf { it.totalPrice }

    val total: Double
        get() = subtotal + mockDeliveryFee

    val totalBackpackUnits: Int
        get() = items.filter { it.physicalSpecs.cargoPlacement == CargoPlacement.THERMAL_BACKPACK || it.physicalSpecs.fragility == FragilityLevel.DELICATE }
            .sumOf { it.physicalSpecs.volumeUnits * it.quantity }

    val totalRackUnits: Int
        get() = items.filter { it.physicalSpecs.cargoPlacement == CargoPlacement.REAR_RACK_STRAPS && it.physicalSpecs.fragility != FragilityLevel.DELICATE }
            .sumOf { it.physicalSpecs.volumeUnits * it.quantity }

    val totalDelicateItems: Int
        get() = items.filter { it.physicalSpecs.fragility == FragilityLevel.DELICATE }
            .sumOf { it.quantity }

    val isCapacityExceeded: Boolean
        get() = totalBackpackUnits > maxBackpackUnits || totalRackUnits > maxRackUnits || totalDelicateItems > maxDelicateItems

    val hasFragileOverload: Boolean
        get() = totalDelicateItems > maxDelicateItems

    val participatingBusinesses: List<String>
        get() = items.map { it.businessName }.distinct()

    val hasHotFood: Boolean
        get() = items.any { it.physicalSpecs.thermalRequirement == ThermalRequirement.HOT }
}
