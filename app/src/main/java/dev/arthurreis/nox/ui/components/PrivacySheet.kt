package dev.arthurreis.nox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.arthurreis.nox.browser.BrowserState
import dev.arthurreis.nox.ui.theme.NoxBorder
import dev.arthurreis.nox.ui.theme.NoxGreen
import dev.arthurreis.nox.ui.theme.NoxRed
import dev.arthurreis.nox.ui.theme.NoxSurfaceRaised
import dev.arthurreis.nox.ui.theme.NoxTextMuted

@Composable
fun PrivacySheet(
    state: BrowserState,
    onShieldChanged: (Boolean) -> Unit,
    onTrackerBlockingChanged: (Boolean) -> Unit,
    onClearData: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (state.shieldEnabled) NoxGreen.copy(alpha = 0.16f) else NoxSurfaceRaised,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = if (state.shieldEnabled) NoxGreen else NoxTextMuted,
                )
            }
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(
                    text = if (state.shieldEnabled) "Escudo ativo" else "Escudo pausado",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = if (state.blockerReady) "uBlock Origin + proteção Nox" else "Inicializando filtros…",
                    color = NoxTextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                value = state.adsSkippedThisSession.toString(),
                label = "anúncios removidos",
                icon = {
                    Icon(Icons.Default.Block, contentDescription = null, tint = NoxRed)
                },
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = state.blockedThisSession.toString(),
                label = "rastreios bloqueados",
                icon = {
                    Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = NoxGreen)
                },
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = NoxBorder)

        SettingSwitch(
            title = "Bloquear anúncios",
            subtitle = "Filtra requisições, banners, promoções e anúncios do player.",
            checked = state.shieldEnabled,
            onCheckedChange = onShieldChanged,
        )
        SettingSwitch(
            title = "Proteção antirrastreamento",
            subtitle = "Isola cookies de terceiros e bloqueia rastreadores conhecidos.",
            checked = state.blockTrackers,
            onCheckedChange = onTrackerBlockingChanged,
        )

        OutlinedButton(
            onClick = onClearData,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NoxTextMuted),
        ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null)
            Text("Apagar sessão, cookies e cache", modifier = Modifier.padding(start = 8.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "O login permanece somente neste aparelho. O Google ainda pode associar a atividade à conta conectada.",
            color = NoxTextMuted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 18.dp),
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(NoxSurfaceRaised, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        icon()
        Spacer(modifier = Modifier.height(12.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, color = NoxTextMuted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                color = NoxTextMuted,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 3.dp, end = 16.dp),
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NoxGreen,
                uncheckedThumbColor = NoxTextMuted,
                uncheckedTrackColor = NoxBorder,
            ),
        )
    }
}
