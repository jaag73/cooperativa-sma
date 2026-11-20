package mx.cooperativasma.app.domain.model

enum class UserRole { CLIENT, BUSINESS, COURIER }
enum class CourierStatus { OFFLINE, ACTIVE_IN_COMMERCIAL_ZONE, AVAILABLE_IN_COMMUNITY, HEADING_HOME }

enum class AccountStatus { PENDING_VERIFICATION, ACTIVE, SUSPENDED }
enum class VerificationStatus { NOT_SUBMITTED, PENDING, APPROVED, REJECTED }

data class MotorcycleSpecs(
    val brand: String = "",
    val model: String = "",
    val year: Int = 0,
    val displacementCc: Int = 0,
    val fuelEfficiencyKmPerL: Double = 0.0,
    val plateNumber: String = ""
)

data class ClientKycData(
    val idDocumentUrl: String = "",
    val selfieUrl: String = "",
    val proofOfAddressUrl: String? = null,
    val verificationStatus: VerificationStatus = VerificationStatus.NOT_SUBMITTED
)

data class MembershipTrial(
    val trialStartDate: String = "",
    val trialEndDate: String = "",
    val isTrialActive: Boolean = true
)

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.CLIENT,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val communityId: String? = null,

    // Beneficio de prueba (Primer mes gratis para Negocio y Repartidor)
    val trialInfo: MembershipTrial? = null,

    // Control de efectivo y KYC
    val cashPaymentAllowed: Boolean = false,
    val clientKyc: ClientKycData? = null,

    // Datos operativos de Repartidor
    val motorcycle: MotorcycleSpecs? = null,
    val courierLicenseUrl: String? = null,
    val circulationCardUrl: String? = null,
    val primaryRestBase: CourierRestBase? = null,
    val secondaryRestBase: CourierRestBase? = null,
    
    // Taxonomía comercial (Micronegocios)
    val taxonomy: BusinessTaxonomy? = null,
    val structuredAddress: StructuredAddress? = null,
    val identityVerification: BusinessIdentityVerification? = null,
    val branding: BusinessBranding? = null,

    // Dispersión bancaria y datos fiscales opcionales (NO obligatorios al inicio)
    val clabe: String? = null,
    val bankName: String? = null,
    val rfc: String? = null,

    val courierStatus: CourierStatus? = null,
    val dailyEarnings: Double = 0.0
)
