package ru.hyper.messenger.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PermMedia
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.hyper.messenger.R
import ru.hyper.messenger.data.DemoData
import ru.hyper.messenger.ui.LocalAppContainer
import ru.hyper.messenger.ui.components.DemoBadge
import ru.hyper.messenger.ui.components.HyperAvatar
import ru.hyper.messenger.ui.components.HyperLogo
import ru.hyper.messenger.ui.components.SectionTitle
import ru.hyper.messenger.ui.components.SettingsDivider
import ru.hyper.messenger.ui.components.SettingsGroup
import ru.hyper.messenger.ui.components.SettingsRow

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onOpenThemes: () -> Unit,
    onOpenQr: () -> Unit,
) {
    val container = LocalAppContainer.current
    val currentUser by container.auth.user.collectAsStateWithLifecycle()
    val user = currentUser ?: DemoData.demoUser
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 14.dp, bottom = 28.dp),
    ) {
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
        )

        Spacer(Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    ),
                ),
        ) {
            HyperLogo(
                size = 72.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .alpha(0.22f),
            )

            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HyperAvatar(name = user.displayName, size = 76.dp)
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                        )
                        if (user.isDemo) {
                            Spacer(Modifier.height(6.dp))
                            DemoBadge(text = stringResource(R.string.profile_demo_badge))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.46f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Text(
                        text = user.status.ifBlank { stringResource(R.string.profile_status_default) },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }

                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = onEditProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_edit))
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatCard(
                value = DemoData.chats.size.toString(),
                label = stringResource(R.string.profile_stat_chats),
                icon = Icons.Rounded.ChatBubble,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                value = "24",
                label = stringResource(R.string.profile_stat_media),
                icon = Icons.Rounded.PermMedia,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                value = "1",
                label = stringResource(R.string.profile_stat_days),
                icon = Icons.Rounded.CalendarMonth,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(18.dp))

        SectionTitle(text = stringResource(R.string.profile_account))
        SettingsGroup {
            SettingsRow(
                icon = Icons.Rounded.AlternateEmail,
                title = stringResource(R.string.profile_email),
                subtitle = user.email,
                onClick = null,
                showChevron = false,
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Rounded.Badge,
                title = stringResource(R.string.profile_username),
                subtitle = user.username,
                onClick = null,
                showChevron = false,
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Rounded.Phone,
                title = stringResource(R.string.profile_phone),
                subtitle = stringResource(R.string.profile_phone_value),
                onClick = null,
                showChevron = false,
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Rounded.CalendarMonth,
                title = stringResource(R.string.profile_member_since),
                subtitle = stringResource(R.string.profile_member_since_value),
                onClick = null,
                showChevron = false,
            )
        }

        Spacer(Modifier.height(18.dp))

        SectionTitle(text = stringResource(R.string.profile_actions))
        SettingsGroup {
            SettingsRow(
                icon = Icons.Rounded.Palette,
                title = stringResource(R.string.profile_action_themes),
                subtitle = stringResource(R.string.profile_action_themes_desc),
                onClick = onOpenThemes,
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Rounded.QrCode2,
                title = stringResource(R.string.profile_action_qr),
                subtitle = stringResource(R.string.profile_action_qr_desc),
                onClick = onOpenQr,
            )
        }

        Spacer(Modifier.height(18.dp))

        SettingsGroup {
            SettingsRow(
                icon = Icons.Rounded.Logout,
                title = stringResource(R.string.profile_logout),
                onClick = { showLogoutDialog = true },
                showChevron = false,
                iconBackground = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.onErrorContainer,
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.profile_demo_note),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.66f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Rounded.Logout, contentDescription = null) },
            title = { Text(stringResource(R.string.logout_title)) },
            text = { Text(stringResource(R.string.logout_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        container.auth.logout()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.logout_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(28.dp),
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}
