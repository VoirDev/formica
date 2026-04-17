package dev.voir.formica.core

/**
 * Convenience factory.
 */
fun <Data : Any> formica(
    adapter: FormicaValidationAdapter<Data>,
    initialData: Data,
    onSubmit: ((Data) -> Unit)? = null
): Formica<Data> = Formica(
    adapter = adapter,
    initialData = initialData,
    onSubmit = onSubmit
)
