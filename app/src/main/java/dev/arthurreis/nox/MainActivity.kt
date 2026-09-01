package dev.arthurreis.nox

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.arthurreis.nox.browser.BrowserViewModel
import dev.arthurreis.nox.ui.NoxApp
import dev.arthurreis.nox.ui.theme.NoxTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var browserViewModel: BrowserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        browserViewModel = runCatching {
            ViewModelProvider(this)[BrowserViewModel::class.java]
        }.getOrElse { error ->
            Log.e(TAG, "GeckoView failed during startup", error)
            showStartupFailure(error)
            return
        }

        observeFullScreen()

        setContent {
            NoxApp(viewModel = browserViewModel)
        }
    }

    private fun showStartupFailure(error: Throwable) {
        val details = generateSequence(error) { it.cause }
            .joinToString(" → ") { it.message ?: it::class.java.simpleName }
            .take(600)
        setContent {
            NoxTheme {
                StartupFailure(details)
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!::browserViewModel.isInitialized) return
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S &&
            browserViewModel.state.value.isFullScreen &&
            !isInPictureInPictureMode
        ) {
            enterPictureInPictureMode(
                buildPictureInPictureParams(),
            )
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (!::browserViewModel.isInitialized) return
        if (!isInPictureInPictureMode && browserViewModel.state.value.isFullScreen) {
            applySystemBars(true)
        }
    }

    private fun observeFullScreen() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                browserViewModel.state.collect { state ->
                    applySystemBars(state.isFullScreen || isInPictureInPictureMode)
                    updatePictureInPictureParams(state.isFullScreen)
                }
            }
        }
    }

    private fun updatePictureInPictureParams(autoEnter: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setPictureInPictureParams(buildPictureInPictureParams(autoEnter))
        }
    }

    private fun buildPictureInPictureParams(
        autoEnter: Boolean = false,
    ): PictureInPictureParams {
        val sourceRect = Rect().also(window.decorView::getGlobalVisibleRect)
        return PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .setSourceRectHint(sourceRect)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setAutoEnterEnabled(autoEnter)
                    setSeamlessResizeEnabled(true)
                }
            }
            .build()
    }

    private fun applySystemBars(enabled: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (enabled) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    companion object {
        private const val TAG = "NoxStartup"
    }
}

@Composable
private fun StartupFailure(details: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        Text("O Nox não conseguiu iniciar", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Esta mensagem substitui o fechamento inesperado e permite identificar a causa.",
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            details.ifBlank { "Falha desconhecida no mecanismo GeckoView." },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
