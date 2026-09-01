package dev.arthurreis.nox.browser

data class BrowserState(
    val title: String = "YouTube",
    val url: String = BrowserViewModel.HOME_URL,
    val progress: Int = 0,
    val isLoading: Boolean = true,
    val isSecure: Boolean = true,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isFullScreen: Boolean = false,
    val shieldEnabled: Boolean = true,
    val blockTrackers: Boolean = true,
    val keepLogin: Boolean = true,
    val blockerReady: Boolean = false,
    val blockedThisSession: Int = 0,
    val adsSkippedThisSession: Int = 0,
    val showWelcome: Boolean = false,
    val errorMessage: String? = null,
)
