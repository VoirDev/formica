package dev.voir.formica

/**
 * A lightweight, reflection-free *lens* describing how to read/write a single field [V] on a model [Data].
 *
 * - [id] must be a stable, unique key (e.g., "firstName"). It's used to store/lookup the field in the form.
 * - [get] reads the current value of the field from [Data].
 * - [set] returns a **new** [Data] instance with the field updated (immutability by design).
 * - [clear] optionally returns a **new** [Data] with the field cleared (e.g., to `null` or default),
 *   used when `onChange(..., value = null)` is invoked. If absent, `null` updates are ignored.
 *
 * This avoids kotlin-reflect and works great for KMP. Define them next to your data model:
 *
 * ```kotlin
 * data class Profile(val firstName: String, val note: String?)
 *
 * val FirstName = FormicaFieldId<Profile, String>(
 *   id = "firstName",
 *   get = { it.firstName },
 *   set = { d, v -> d.copy(firstName = v) }
 * )
 *
 * val Note = FormicaFieldId<Profile, String?>(
 *   id = "note",
 *   get = { it.note },
 *   set = { d, v -> d.copy(note = v) },
 *   clear = { d -> d.copy(note = null) }
 * )
 * ```
 */
class FormicaFieldId<Data, V>(
    val id: String,                       // Stable key (e.g., "firstName")
    val get: (Data) -> V,                 // Read from Data
    val set: (Data, V) -> Data,           // Return a *new* Data with V set (immutable update)
    val clear: ((Data) -> Data)? = null   // Optional immutable "clear" operation
)
