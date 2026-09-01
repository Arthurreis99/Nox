package dev.arthurreis.nox.browser

import android.content.Context
import org.mozilla.geckoview.ContentBlocking
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings

object NoxRuntime {
    @Volatile
    private var instance: GeckoRuntime? = null

    fun prepare(context: Context) {
        get(context)
    }

    fun get(context: Context): GeckoRuntime =
        instance ?: synchronized(this) {
            instance ?: create(context.applicationContext).also { instance = it }
        }

    private fun create(context: Context): GeckoRuntime {
        val contentBlocking = ContentBlocking.Settings.Builder()
            .antiTracking(ContentBlocking.AntiTracking.STRICT)
            .safeBrowsing(ContentBlocking.SafeBrowsing.DEFAULT)
            .cookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_FIRST_PARTY_AND_ISOLATE_OTHERS)
            .cookieBehaviorPrivateMode(ContentBlocking.CookieBehavior.ACCEPT_NONE)
            .enhancedTrackingProtectionLevel(ContentBlocking.EtpLevel.STRICT)
            .enhancedTrackingProtectionCategory(ContentBlocking.EtpCategory.STRICT)
            .cookiePurging(true)
            .emailTrackerBlockingPrivateMode(true)
            .queryParameterStrippingEnabled(true)
            .queryParameterStrippingPrivateBrowsingEnabled(true)
            .queryParameterStrippingStripList(
                "utm_source",
                "utm_medium",
                "utm_campaign",
                "utm_term",
                "utm_content",
                "gclid",
                "dclid",
                "fbclid",
                "msclkid",
                "mc_cid",
                "mc_eid",
            )
            .build()

        val settings = GeckoRuntimeSettings.Builder()
            .javaScriptEnabled(true)
            .globalPrivacyControlEnabled(true)
            // Keep Gecko's remote debugger disabled even in sideloadable debug builds.
            .remoteDebuggingEnabled(false)
            .extensionsProcessEnabled(true)
            .contentBlocking(contentBlocking)
            .build()

        return GeckoRuntime.create(context, settings)
    }
}
