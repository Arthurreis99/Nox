package dev.arthurreis.nox.browser

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dev.arthurreis.nox.data.PrivacyPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.ContentBlocking
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.StorageController
import org.mozilla.geckoview.WebExtension
import org.mozilla.geckoview.WebExtensionController

class BrowserViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = PrivacyPreferences(application)
    private val runtime = NoxRuntime.get(application)
    private val extensionController = runtime.webExtensionController

    private var uBlockExtension: WebExtension? = null
    private var noxShieldExtension: WebExtension? = null
    private var hasLoadedHome = false

    private val _state = MutableStateFlow(
        BrowserState(
            shieldEnabled = preferences.shieldEnabled,
            blockTrackers = preferences.blockTrackers,
            keepLogin = preferences.keepLogin,
            showWelcome = !preferences.firstRunComplete,
        ),
    )
    val state: StateFlow<BrowserState> = _state.asStateFlow()

    val session: GeckoSession = GeckoSession(
        GeckoSessionSettings.Builder()
            .useTrackingProtection(true)
            .allowJavascript(true)
            .userAgentMode(GeckoSessionSettings.USER_AGENT_MODE_MOBILE)
            .viewportMode(GeckoSessionSettings.VIEWPORT_MODE_MOBILE)
            .suspendMediaWhenInactive(false)
            .build(),
    )

    init {
        configureSession()
        session.open(runtime)
        installBlockers()
    }

    fun loadHome() {
        session.loadUri(HOME_URL)
        hasLoadedHome = true
    }

    fun ensureHomeLoaded() {
        if (!hasLoadedHome && _state.value.blockerReady) loadHome()
    }

    fun goBack() {
        when {
            _state.value.isFullScreen -> session.exitFullScreen()
            _state.value.canGoBack -> session.goBack()
        }
    }

    fun goForward() {
        if (_state.value.canGoForward) session.goForward()
    }

    fun reload() {
        session.reload()
    }

    fun stop() {
        session.stop()
    }

    fun dismissWelcome() {
        preferences.firstRunComplete = true
        _state.update { it.copy(showWelcome = false) }
    }

    fun setShieldEnabled(enabled: Boolean) {
        preferences.shieldEnabled = enabled
        _state.update { it.copy(shieldEnabled = enabled, errorMessage = null) }

        val action: (WebExtension) -> GeckoResult<WebExtension> = { extension ->
            if (enabled) {
                extensionController.enable(extension, WebExtensionController.EnableSource.USER)
            } else {
                extensionController.disable(extension, WebExtensionController.EnableSource.USER)
            }
        }

        listOfNotNull(uBlockExtension, noxShieldExtension).forEach { extension ->
            action(extension).accept(
                { updated ->
                    updated?.let {
                        if (it.id == UBLOCK_ID) uBlockExtension = it
                        if (it.id == NOX_SHIELD_ID) noxShieldExtension = it
                    }
                },
                { reportError("Não foi possível alterar o escudo", it) },
            )
        }
        reload()
    }

    fun setBlockTrackers(enabled: Boolean) {
        preferences.blockTrackers = enabled
        _state.update { it.copy(blockTrackers = enabled) }
        session.settings.useTrackingProtection = enabled
        reload()
    }

    fun setKeepLogin(enabled: Boolean) {
        preferences.keepLogin = enabled
        _state.update { it.copy(keepLogin = enabled) }
        if (!enabled) clearBrowsingData()
    }

    fun clearBrowsingData(onComplete: () -> Unit = {}) {
        runtime.storageController.clearData(StorageController.ClearFlags.ALL).accept(
            {
                session.purgeHistory()
                _state.update {
                    it.copy(
                        blockedThisSession = 0,
                        adsSkippedThisSession = 0,
                        errorMessage = null,
                    )
                }
                loadHome()
                onComplete()
            },
            { reportError("Não foi possível limpar os dados", it) },
        )
    }

    fun consumeError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun configureSession() {
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean,
            ) {
                url ?: return
                _state.update { it.copy(url = url, isSecure = url.startsWith("https://")) }
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                _state.update { it.copy(canGoBack = canGoBack) }
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                _state.update { it.copy(canGoForward = canGoForward) }
            }

            override fun onLoadRequest(
                session: GeckoSession,
                request: GeckoSession.NavigationDelegate.LoadRequest,
            ): GeckoResult<AllowOrDeny>? {
                return if (NavigationPolicy.isAllowed(request.uri)) {
                    null
                } else {
                    GeckoResult.deny()
                }
            }
        }

        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                _state.update { it.copy(isLoading = true, progress = 0, errorMessage = null) }
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        progress = 100,
                        errorMessage = if (success) it.errorMessage else "A página não pôde ser carregada",
                    )
                }
            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                _state.update { it.copy(progress = progress.coerceIn(0, 100)) }
            }

            override fun onSecurityChange(
                session: GeckoSession,
                securityInfo: GeckoSession.ProgressDelegate.SecurityInformation,
            ) {
                _state.update { it.copy(isSecure = securityInfo.isSecure) }
            }
        }

        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                _state.update { it.copy(title = title?.takeIf(String::isNotBlank) ?: "YouTube") }
            }

            override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
                _state.update { it.copy(isFullScreen = fullScreen) }
            }

            override fun onCrash(session: GeckoSession) {
                reportError("O mecanismo de navegação foi reiniciado")
                runCatching {
                    session.close()
                    session.open(runtime)
                    loadHome()
                }
            }
        }

        session.contentBlockingDelegate = object : ContentBlocking.Delegate {
            override fun onContentBlocked(session: GeckoSession, event: ContentBlocking.BlockEvent) {
                _state.update { it.copy(blockedThisSession = it.blockedThisSession + 1) }
            }
        }
    }

    private fun installBlockers() {
        extensionController.ensureBuiltIn(UBLOCK_PATH, UBLOCK_ID).accept(
            { extension ->
                if (extension == null) {
                    reportError("O bloqueador principal retornou uma instalação inválida")
                } else {
                    uBlockExtension = extension
                    if (!preferences.shieldEnabled) {
                        extensionController.disable(extension, WebExtensionController.EnableSource.APP)
                    }
                }
                installNoxShield()
            },
            { error ->
                reportError("O bloqueador principal não pôde ser iniciado", error)
                installNoxShield()
            },
        )
    }

    private fun installNoxShield() {
        extensionController.ensureBuiltIn(NOX_SHIELD_PATH, NOX_SHIELD_ID).accept(
            { extension ->
                if (extension == null) {
                    reportError("A proteção complementar retornou uma instalação inválida")
                } else {
                    noxShieldExtension = extension
                    extension.setMessageDelegate(extensionMessageDelegate, NATIVE_APP)
                    if (!preferences.shieldEnabled) {
                        extensionController.disable(extension, WebExtensionController.EnableSource.APP)
                    }
                }
                _state.update { it.copy(blockerReady = true) }
                ensureHomeLoaded()
            },
            {
                reportError("A proteção complementar não pôde ser iniciada", it)
                _state.update { state -> state.copy(blockerReady = true) }
                ensureHomeLoaded()
            },
        )
    }

    private val extensionMessageDelegate = object : WebExtension.MessageDelegate {
        override fun onMessage(
            nativeApp: String,
            message: Any,
            sender: WebExtension.MessageSender,
        ): GeckoResult<Any>? {
            if (nativeApp != NATIVE_APP || message !is JSONObject) return null

            when (message.optString("type")) {
                "blocked" -> _state.update {
                    it.copy(
                        blockedThisSession = it.blockedThisSession + message.optInt("count", 1)
                            .coerceAtLeast(1),
                    )
                }

                "adSkipped" -> _state.update {
                    it.copy(adsSkippedThisSession = it.adsSkippedThisSession + 1)
                }
            }
            return GeckoResult.fromValue(JSONObject().put("ok", true))
        }
    }

    private fun reportError(message: String, throwable: Throwable? = null) {
        throwable?.printStackTrace()
        _state.update { it.copy(errorMessage = message) }
    }

    override fun onCleared() {
        if (!preferences.keepLogin) {
            runtime.storageController.clearData(StorageController.ClearFlags.ALL)
        }
        if (session.isOpen) session.close()
    }

    companion object {
        const val HOME_URL = "https://m.youtube.com/"
        private const val UBLOCK_PATH = "resource://android/assets/extensions/ublock/"
        private const val UBLOCK_ID = "uBlock0@raymondhill.net"
        private const val NOX_SHIELD_PATH = "resource://android/assets/extensions/noxshield/"
        private const val NOX_SHIELD_ID = "noxshield@arthurreis.dev"
        private const val NATIVE_APP = "nox"
    }
}
