package mx.cooperativasma.app.domain.model

data class GeoLocation(
    val latitude: Double = 20.9144, // Coordenadas base San Miguel de Allende
    val longitude: Double = -100.7452
)

data class StructuredAddress(
    val country: String = "México",
    val state: String = "Guanajuato",
    val city: String = "San Miguel de Allende",
    val postalCode: String = "",
    val neighborhoodOrCommunity: String = "",
    val isCustomCommunity: Boolean = false,
    val customCommunityProposed: String? = null,
    val isApprovedByAdmin: Boolean = true,
    val street: String = "",
    val exteriorNumber: String = "",
    val interiorNumber: String? = null,
    val coordinates: GeoLocation = GeoLocation()
)

data class BusinessIdentityVerification(
    val ownerIdDocumentUrl: String = "",
    val ownerSelfieUrl: String = "",
    val isVerified: Boolean = false
)

data class BusinessBranding(
    val slogan: String = "",
    val logoUrl: String = "",
    val facadePhotoUrl: String = ""
)
