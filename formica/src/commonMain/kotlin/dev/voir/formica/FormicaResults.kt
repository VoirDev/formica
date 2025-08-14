package dev.voir.formica

sealed class FormicaResult {
    data object NoInput : FormicaResult()
    data object Valid : FormicaResult()
    data class Error(
        val message: String,
        val fieldErrors: Map<String, String> = emptyMap()
    ) : FormicaResult()
}

sealed class FormicaFieldResult {
    data object Success : FormicaFieldResult()

    data class Error(val message: String) : FormicaFieldResult()

    data object NoInput : FormicaFieldResult()
}
