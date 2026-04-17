package dev.voir.formica.compose

import androidx.compose.runtime.compositionLocalOf
import dev.voir.formica.core.Formica

/**
 * Internal CompositionLocal used by Formica scope APIs.
 *
 * Stored as Any? because CompositionLocal cannot be generic itself.
 */
internal val LocalFormica = compositionLocalOf<Formica<Any>?> { null }
