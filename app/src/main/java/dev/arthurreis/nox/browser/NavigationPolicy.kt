package dev.arthurreis.nox.browser

import java.net.URI

internal object NavigationPolicy {
    private val allowedSchemes = setOf("https", "about", "moz-extension")

    fun isAllowed(url: String): Boolean = runCatching {
        URI(url).scheme?.lowercase() in allowedSchemes
    }.getOrDefault(false)
}
