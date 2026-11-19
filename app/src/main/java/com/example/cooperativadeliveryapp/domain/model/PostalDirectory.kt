package com.example.cooperativadeliveryapp.domain.model

data class CommunityMatch(
    val neighborhoodOrCommunity: String,
    val postalCode: String
)

object SmaPostalDirectory {
    const val SUPPORTED_MUNICIPALITY = "San Miguel de Allende"
    const val SUPPORTED_STATE = "Guanajuato"
    const val SUPPORTED_COUNTRY = "México"

    val POSTAL_CODE_CATALOG: Map<String, List<String>> = mapOf(
        // Zona Centro Histórico y alrededores inmediatos
        "37700" to listOf(
            "Centro",
            "San Rafael",
            "Guadalupe",
            "Aurora",
            "Allende",
            "Providencia",
            "La Luz",
            "San Antonio Poniente"
        ),

        // Fraccionamientos, corredores y colonias urbanas consolidadas
        "37710" to listOf(
            "San Antonio",
            "Independencia",
            "Palma",
            "El Paraíso",
            "Mexiquito",
            "Santa Julia",
            "San Luis Rey",
            "Cuevitas"
        ),

        // Comunidades poniente, corredor Atotonilco y ribera
        "37712" to listOf(
            "Santas Marías",
            "Atotonilco",
            "La Cieneguita",
            "Montecinos",
            "Cruz del Palmar",
            "San Miguel Viejo",
            "Banda",
            "Tirado",
            "San Isidro de la Bandera"
        ),

        // Corredor Alcocer, salida a Celaya y comunidades aledañas
        "37713" to listOf(
            "Alcocer",
            "Rancho Nuevo",
            "Sosnabar",
            "La Cuadrilla",
            "El Mirador",
            "Nuevo Pantoja"
        ),

        // Zona Sur, San Antonio Abad y Ojo de Agua
        "37720" to listOf(
            "San Antonio Abad",
            "La Palmita",
            "Ojo de Agua",
            "Montes de Loreto",
            "Avisperos"
        ),

        // Salida a Querétaro, Valle del Maíz y zona comercial sur
        "37730" to listOf(
            "Valle del Maíz",
            "Salida a Querétaro",
            "La Lejona",
            "La Lejona 2a Sección",
            "Villa de los Frailes",
            "El Caracol",
            "Arroyo Seco"
        ),

        // Zona Norte urbana y fraccionamientos altos
        "37740" to listOf(
            "Insurgentes",
            "San Felipe Neri",
            "Lomas de San José",
            "Las Américas",
            "Ignacio Ramírez",
            "Olimpo"
        ),

        // Corredor Los Rodríguez, carretera a Dolores y comunidades oriente
        "37750" to listOf(
            "Los Rodríguez",
            "Malquinto",
            "Puerto de Nieto",
            "Clavellinas",
            "Galvanes",
            "San José de Gracia",
            "Cañada de Caracheo"
        ),

        // Comunidades rurales sur y sureste
        "37760" to listOf(
            "Tierra Blanca",
            "Galinda",
            "San Lucas",
            "Don Diego",
            "El Lindero",
            "Pozo de Balderas"
        ),

        // Corredores suburbanos y comunidades de transición
        "37770" to listOf(
            "Taboada",
            "Marroquín",
            "Presa Allende",
            "Cieneguita Poniente"
        ),

        // Comunidades limítrofes y rurales adicionales del municipio
        "37780" to listOf(
            "San Antonio de la Joya",
            "El Fraile",
            "Fajardo de Bocas",
            "Jalpa",
            "La Talega"
        )
    )

    fun getColoniasForPostalCode(cp: String): List<String> {
        return POSTAL_CODE_CATALOG[cp.trim()] ?: emptyList()
    }

    fun getAllSupportedPostalCodes(): List<String> {
        return POSTAL_CODE_CATALOG.keys.toList().sorted()
    }

    fun searchCommunities(query: String): List<CommunityMatch> {
        if (query.trim().length < 2) return emptyList()
        val normalizedQuery = query.trim().lowercase()
        val results = mutableListOf<CommunityMatch>()

        POSTAL_CODE_CATALOG.forEach { (cp, colonias) ->
            colonias.forEach { colonia ->
                if (colonia.lowercase().contains(normalizedQuery)) {
                    results.add(CommunityMatch(neighborhoodOrCommunity = colonia, postalCode = cp))
                }
            }
        }
        return results.sortedBy { it.neighborhoodOrCommunity }
    }

    fun findPostalCodeForCommunity(communityName: String): String? {
        POSTAL_CODE_CATALOG.forEach { (cp, colonias) ->
            if (colonias.any { it.equals(communityName.trim(), ignoreCase = true) }) {
                return cp
            }
        }
        return null
    }
}
