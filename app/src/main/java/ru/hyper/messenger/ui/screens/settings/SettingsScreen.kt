package ru.hyper.messenger.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.hyper.messenger.BuildConfig
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
fun SettingsScreen(
    onOpenProfile: () -> Unit,
    onOpenThemes: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenQr: () -> Unit,
    onOpenWebSettings: () -> Unit,
) {
    val container = LocalAppContainer.current
    val currentUser by container.auth.user.collectAsStateWithLifecycle()
    val user = currentUser ?: DemoData.demoUser
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 14.dp,
            bottom = 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = stringResource(R.string.settings_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HyperLogo(size = 42.dp)
            }
        }

        item(key = "mini-profile") {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenProfile),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HyperAvatar(name = user.displayName, size = 58.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.displayName,
                            style = MaterialTheme.typography.titleLarge,
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
                            DemoBadge(text = stringResource(R.string.settings_demo_badge))
                        }
                    }
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                }
            }
        }

        item(key = "appearance-title") {
            SectionTitle(
                text = stringResource(R.string.settings_section_appearance),
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        item(key = "appearance-group") {
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Rounded.Palette,
                    title = stringResource(R.string.settings_themes),
                    subtitle = stringResource(R.string.settings_themes_desc),
                    onClick = onOpenThemes,
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Notifications,
                    title = stringResource(R.string.settings_notifications),
                    subtitle = stringResource(R.string.settings_notifications_desc),
                    onClick = onOpenNotifications,
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Lock,
                    title = stringResource(R.string.settings_privacy),
                    subtitle = stringResource(R.string.settings_privacy_desc),
                    onClick = onOpenPrivacy,
                )
            }
        }

        item(key = "security-title") {
            SectionTitle(
                text = stringResource(R.string.settings_section_security),
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        item(key = "security-group") {
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Rounded.QrCode2,
                    title = stringResource(R.string.settings_qr),
                    subtitle = stringResource(R.string.settings_qr_desc),
                    onClick = onOpenQr,
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Devices,
                    title = stringResource(R.string.settings_devices),
                    subtitle = null,
                    onClick = null,
                    trailing = {
                        Text(
                            text = stringResource(R.string.settings_devices_value),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }
        }

        item(key = "account-title") {
            SectionTitle(
                text = stringResource(R.string.settings_section_account),
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        item(key = "account-group") {
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Rounded.Language,
                    title = stringResource(R.string.settings_web),
                    subtitle = stringResource(R.string.settings_web_desc),
                    onClick = onOpenWebSettings,
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Language,
                    title = stringResource(R.string.profile_language),
                    subtitle = null,
                    onClick = null,
                    trailing = {
                        Text(
                            text = stringResource(R.string.profile_language_value),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }
        }

        item(key = "about-title") {
            SectionTitle(
                text = stringResource(R.string.settings_section_about),
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        item(key = "about-group") {
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.settings_about),
                    subtitle = stringResource(R.string.settings_about_desc),
                    onClick = { showAboutDialog = true },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.settings_version),
                    subtitle = null,
                    onClick = null,
                    trailing = {
                        Text(
                            text = "${BuildConfig.VERSION_NAME} · ${BuildConfig.VERSION_CODE}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }
        }

        item(key = "logout") {
            Spacer(Modifier.height(4.dp))
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Rounded.Logout,
                    title = stringResource(R.string.settings_logout),
                    onClick = { showLogoutDialog = true },
                    showChevron = false,
                    iconBackground = MaterialTheme.colorScheme.errorContainer,
                    iconTint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }

        item(key = "footer") {
            Text(
                text = stringResource(R.string.settings_made_with),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
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

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { HyperLogo(size = 64.dp) },
            title = {
                Text(
                    text = stringResource(R.string.settings_about_dialog_title),
                    fontWeight = FontWeight.ExtraBold,
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.settings_about_dialog_body),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "v${BuildConfig.VERSION_NAME} · ${BuildConfig.VERSION_CODE}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            },
            shape = RoundedCornerShape(28.dp),
        )
    }
}
