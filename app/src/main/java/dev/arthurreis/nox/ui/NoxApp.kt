package dev.arthurreis.nox.ui

import android.graphics.Color
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.arthurreis.nox.browser.BrowserViewModel
import dev.arthurreis.nox.ui.components.NavigationSheet
import dev.arthurreis.nox.ui.components.NoxTopBar
import dev.arthurreis.nox.ui.components.PrivacySheet
import dev.arthurreis.nox.ui.theme.NoxBackground
import dev.arthurreis.nox.ui.theme.NoxSurface
import dev.arthurreis.nox.ui.theme.NoxTheme
import org.mozilla.geckoview.GeckoView

private enum class OpenSheet { Privacy, Navigation }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoxApp(viewModel: BrowserViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var openSheet by remember { mutableStateOf<OpenSheet?>(null) }

    LaunchedEffect(state.blockerReady) {
        viewModel.ensureHomeLoaded()
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    BackHandler(enabled = state.isFullScreen || state.canGoBack || openSheet != null) {
        if (openSheet != null) {
            openSheet = null
        } else {
            viewModel.goBack()
        }
    }

    NoxTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = NoxBackground,
            snackbarHost = { SnackbarHost(snackbar) },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (state.isFullScreen) androidx.compose.foundation.layout.PaddingValues() else padding),
            ) {
                if (!state.isFullScreen) {
                    NoxTopBar(
                        state = state,
                        onBack = viewModel::goBack,
                        onHome = viewModel::loadHome,
                        onShield = { openSheet = OpenSheet.Privacy },
                        onMenu = { openSheet = OpenSheet.Navigation },
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(NoxBackground),
                ) {
                    AndroidView(
                        factory = { context ->
                            GeckoView(context).apply {
                                setBackgroundColor(Color.BLACK)
                                setSession(viewModel.session)
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        onRelease = { view -> view.releaseSession() },
                    )
                }
            }
        }

        if (openSheet != null) {
            ModalBottomSheet(
                onDismissRequest = { openSheet = null },
                sheetState = sheetState,
                containerColor = NoxSurface,
            ) {
                when (openSheet) {
                    OpenSheet.Privacy -> PrivacySheet(
                        state = state,
                        onShieldChanged = viewModel::setShieldEnabled,
                        onTrackerBlockingChanged = viewModel::setBlockTrackers,
                        onClearData = {
                            viewModel.clearBrowsingData { openSheet = null }
                        },
                    )

                    OpenSheet.Navigation -> NavigationSheet(
                        state = state,
                        onHome = {
                            viewModel.loadHome()
                            openSheet = null
                        },
                        onForward = {
                            viewModel.goForward()
                            openSheet = null
                        },
                        onReload = {
                            viewModel.reload()
                            openSheet = null
                        },
                        onKeepLoginChanged = viewModel::setKeepLogin,
                    )

                    null -> Unit
                }
            }
        }

        if (state.showWelcome) {
            WelcomeDialog(onContinue = viewModel::dismissWelcome)
        }
    }
}

@Composable
private fun WelcomeDialog(onContinue: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Bem-vindo ao Nox") },
        text = {
            Text(
                "O escudo já começa ativo. Ele combina uBlock Origin, proteção antirrastreamento do Firefox e filtros próprios para o player do YouTube. Nenhum dado de navegação é enviado para servidores do Nox.",
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            Button(onClick = onContinue) {
                Text("Começar protegido")
            }
        },
        dismissButton = {
            TextButton(onClick = onContinue) {
                Text("Entendi")
            }
        },
    )
}
