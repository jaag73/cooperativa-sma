package com.example.cooperativadeliveryapp.domain.model

data class BusinessTaxonomy(
    val sector: String = "Restaurante / Comida",
    val category: String = "Otra (sugerir)",
    val customSectorProposed: String? = null,
    val customCategoryProposed: String? = null,
    val isApprovedByAdmin: Boolean = true
)

val DEFAULT_BUSINESS_SECTORS = listOf(
    "Restaurante / Comida",
    "Abarrotes y Minisúper",
    "Frutas y Verduras",
    "Panadería y Repostería",
    "Bebidas y Paletería",
    "Farmacia y Salud",
    "Otro (sugerir giro)"
)

val SECTOR_DEFAULT_CATEGORIES = mapOf(
    "Restaurante / Comida" to listOf("Tortas", "Tacos y Antojitos", "Comida Corrida", "Pizzas y Hamburguesas", "Mariscos", "Vegetariana / Vegana", "Otra (sugerir)"),
    "Abarrotes y Minisúper" to listOf("Lácteos y Huevos", "Enlatados y Despensa", "Botanas y Dulces", "Bebidas", "Limpieza del Hogar", "Otra (sugerir)"),
    "Frutas y Verduras" to listOf("Frutas de Temporada", "Verduras y Legumbres", "Hierbas y Especias", "Semillas y Frutos Secos", "Otra (sugerir)"),
    "Panadería y Repostería" to listOf("Pan Dulce", "Bolillo y Telera", "Pasteles y Pays", "Galletas", "Otra (sugerir)")
)
