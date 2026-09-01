package dev.arthurreis.nox.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.arthurreis.nox.browser.BrowserState
import dev.arthurreis.nox.ui.theme.NoxBorder
import dev.arthurreis.nox.ui.theme.NoxSurface
import dev.arthurreis.nox.ui.theme.NoxTextMuted

@Composable
fun NavigationSheet(
    state: BrowserState,
    onHome: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit,
    onKeepLoginChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text("Nox", style = MaterialTheme.typography.titleLarge)
        Text(
            "Navegador dedicado, privado e sem telemetria.",
            color = NoxTextMuted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 2.dp, bottom = 18.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalButton(onClick = onHome, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Home, contentDescription = null)
                Text("Início", modifier = Modifier.padding(start = 6.dp))
            }
            FilledTonalButton(
                onClick = onForward,
                enabled = state.canGoForward,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                Text("Avançar", modifier = Modifier.padding(start = 6.dp))
            }
            FilledTonalButton(onClick = onReload, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Text("Recarregar", modifier = Modifier.padding(start = 6.dp))
            }
        }

        HorizontalDivider(color = NoxBorder, modifier = Modifier.padding(top = 18.dp))

        ListItem(
            headlineContent = { Text("Manter login") },
            supportingContent = {
                Text("Preserva a sessão do YouTube. Ao desativar, os dados locais são apagados imediatamente.")
            },
            leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
            trailingContent = {
                Switch(checked = state.keepLogin, onCheckedChange = onKeepLoginChanged)
            },
            colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = NoxSurface),
        )

        Text(
            text = "Nox 0.1.1 • GeckoView • uBlock Origin 1.74.0",
            color = NoxTextMuted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 18.dp),
        )
    }
}
