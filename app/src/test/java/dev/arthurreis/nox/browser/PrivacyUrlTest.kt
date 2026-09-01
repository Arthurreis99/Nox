package dev.arthurreis.nox.browser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyUrlTest {
    @Test
    fun youtubeAndGoogleAccountUrlsAreAllowed() {
        assertTrue(NavigationPolicy.isAllowed("https://m.youtube.com/watch?v=abc"))
        assertTrue(NavigationPolicy.isAllowed("https://accounts.google.com/signin"))
        assertTrue(NavigationPolicy.isAllowed("about:blank"))
        assertTrue(NavigationPolicy.isAllowed("moz-extension://nox/options.html"))
    }

    @Test
    fun unsafeSchemesAreRejected() {
        assertFalse(NavigationPolicy.isAllowed("intent://watch/#Intent;scheme=youtube;end"))
        assertFalse(NavigationPolicy.isAllowed("file:///sdcard/private.txt"))
        assertFalse(NavigationPolicy.isAllowed("javascript:alert(1)"))
        assertFalse(NavigationPolicy.isAllowed("not a URL"))
    }

    @Test
    fun homeUrlUsesEncryptedMobileYouTube() {
        assertEquals("https://m.youtube.com/", BrowserViewModel.HOME_URL)
    }
}
