package dev.voir.formica.sample

data class DemoFormData(
    val email: String = "",
    val name: String = "",
    val nickname: String? = null,
    val password: String = "",
    val website: String? = null,
    val age: Int? = null,
    val score: Double? = null,
    val acceptedTerms: Boolean = false,
    val hasSecondaryAddress: Boolean = false,
    val secondaryAddress: String? = null,
    val manualValidationField: String = ""
)
