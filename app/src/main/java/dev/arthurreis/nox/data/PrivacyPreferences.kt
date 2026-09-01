package dev.arthurreis.nox.data

import android.content.Context
import androidx.core.content.edit

class PrivacyPreferences(context: Context) {
    private val store = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var shieldEnabled: Boolean
        get() = store.getBoolean(KEY_SHIELD_ENABLED, true)
        set(value) = store.edit { putBoolean(KEY_SHIELD_ENABLED, value) }

    var blockTrackers: Boolean
        get() = store.getBoolean(KEY_BLOCK_TRACKERS, true)
        set(value) = store.edit { putBoolean(KEY_BLOCK_TRACKERS, value) }

    var keepLogin: Boolean
        get() = store.getBoolean(KEY_KEEP_LOGIN, true)
        set(value) = store.edit { putBoolean(KEY_KEEP_LOGIN, value) }

    var firstRunComplete: Boolean
        get() = store.getBoolean(KEY_FIRST_RUN_COMPLETE, false)
        set(value) = store.edit { putBoolean(KEY_FIRST_RUN_COMPLETE, value) }

    companion object {
        private const val FILE_NAME = "nox_privacy"
        private const val KEY_SHIELD_ENABLED = "shield_enabled"
        private const val KEY_BLOCK_TRACKERS = "block_trackers"
        private const val KEY_KEEP_LOGIN = "keep_login"
        private const val KEY_FIRST_RUN_COMPLETE = "first_run_complete"
    }
}
