package dev.arthurreis.nox.ui.components

import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.arthurreis.nox.browser.BrowserState
import dev.arthurreis.nox.ui.theme.NoxBorder
import dev.arthurreis.nox.ui.theme.NoxGreen
import dev.arthurreis.nox.ui.theme.NoxRed
import dev.arthurreis.nox.ui.theme.NoxSurface
import dev.arthurreis.nox.ui.theme.NoxTextMuted

@Composable
fun NoxTopBar(
    state: BrowserState,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onShield: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.background(NoxSurface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.canGoBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onHome)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(31.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(NoxRed),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "N",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.isSecure) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = NoxGreen,
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        Text(
                            text = hostLabel(state.url),
                            color = NoxTextMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = onShield) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Escudo de privacidade",
                        tint = if (state.shieldEnabled && state.blockerReady) NoxGreen else NoxTextMuted,
                    )
                }
                val total = state.blockedThisSession + state.adsSkippedThisSession
                if (total > 0) {
                    Badge(
                        modifier = Modifier.padding(top = 5.dp, end = 3.dp),
                        containerColor = NoxRed,
                    ) {
                        Text(if (total > 99) "99+" else total.toString())
                    }
                }
            }

            IconButton(onClick = onMenu) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Mais opções")
            }
        }

        if (state.isLoading) {
            LinearProgressIndicator(
                progress = { state.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = NoxRed,
                trackColor = NoxBorder,
            )
        } else {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NoxBorder),
            )
        }
    }
}

private fun hostLabel(url: String): String = runCatching {
    url.toUri().host?.removePrefix("www.") ?: "navegação protegida"
}.getOrDefault("navegação protegida")
