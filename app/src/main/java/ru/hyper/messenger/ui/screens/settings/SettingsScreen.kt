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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Security
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
import ru.hyper.messenger.core.ServerHost
import ru.hyper.messenger.ui.LocalAppContainer
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
    onOpenSecurity: () -> Unit,
    onOpenWebSettings: () -> Unit,
) {
    val container = LocalAppContainer.current
    val user by container.auth.user.collectAsStateWithLifecycle()
    val serverHost by container.preferences.serverHost.collectAsStateWithLifecycle()
    val account = user ?: return
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var showServer by rememberSaveable { mutableStateOf(false) }
    var showLogout by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp, 14.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text(stringResource(R.string.settings_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                HyperLogo(size = 42.dp)
            }
        }
        item {
            Surface(
                Modifier.fillMaxWidth().clickable(onClick = onOpenProfile),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    HyperAvatar(account.displayName, size = 58.dp, imageUrl = account.customAvatar.takeIf { it.isNotBlank() })
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            account.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            account.username,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                        )
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
            }
        }
        item { SectionTitle(stringResource(R.string.settings_section_appearance)) }
        item {
            SettingsGroup {
                SettingsRow(Icons.Rounded.Palette, stringResource(R.string.settings_themes), subtitle = stringResource(R.string.settings_themes_desc), onClick = onOpenThemes)
                SettingsDivider()
                SettingsRow(Icons.Rounded.Notifications, stringResource(R.string.settings_notifications), subtitle = stringResource(R.string.settings_notifications_desc), onClick = onOpenNotifications)
                SettingsDivider()
                SettingsRow(Icons.Rounded.Lock, stringResource(R.string.settings_privacy), subtitle = stringResource(R.string.settings_privacy_desc), onClick = onOpenPrivacy)
            }
        }
        item { SectionTitle(stringResource(R.string.settings_section_security)) }
        item {
            SettingsGroup {
                SettingsRow(Icons.Rounded.Security, stringResource(R.string.settings_security), subtitle = stringResource(R.string.settings_security_desc), onClick = onOpenSecurity)
            }
        }
        item { SectionTitle(stringResource(R.string.settings_section_account)) }
        item {
            SettingsGroup {
                SettingsRow(
                    Icons.Rounded.Dns, stringResource(R.string.settings_server),
                    subtitle = stringResource(R.string.settings_server_desc),
                    onClick = { showServer = true },
                    trailing = {
                        Text(serverHost.host, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    },
                )
                SettingsDivider()
                SettingsRow(Icons.Rounded.Language, stringResource(R.string.settings_web), subtitle = stringResource(R.string.settings_web_desc), onClick = onOpenWebSettings)
            }
        }
        item { SectionTitle(stringResource(R.string.settings_section_about)) }
        item {
            SettingsGroup {
                SettingsRow(Icons.Rounded.Info, stringResource(R.string.settings_about), subtitle = stringResource(R.string.settings_about_desc), onClick = { showAbout = true })
                SettingsDivider()
                SettingsRow(
                    Icons.Rounded.Info, stringResource(R.string.settings_version),
                    onClick = null,
                    trailing = { Text("v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                )
                SettingsDivider()
                SettingsRow(
                    Icons.Rounded.Devices, stringResource(R.string.settings_min_android),
                    onClick = null,
                    trailing = { Text(stringResource(R.string.settings_min_android_value), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                )
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            SettingsGroup {
                SettingsRow(
                    Icons.Rounded.Logout, stringResource(R.string.settings_logout),
                    onClick = { showLogout = true }, showChevron = false,
                    iconBackground = MaterialTheme.colorScheme.errorContainer,
                    iconTint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
        item {
            Text(
                stringResource(R.string.settings_made_with),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }

    if (showServer) {
        AlertDialog(
            onDismissRequest = { showServer = false },
            icon = { Icon(Icons.Rounded.Dns, contentDescription = null) },
            title = { Text(stringResource(R.string.server_dialog_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.server_dialog_body), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    ServerHost.entries.forEach { host ->
                        val selected = host == serverHost
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                container.preferences.setServerHost(host)
                                showServer = false
                            }.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(host.title, style = MaterialTheme.typography.titleSmall)
                                Text(host.host, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (selected) Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showServer = false }) { Text(stringResource(R.string.close)) } },
            shape = RoundedCornerShape(28.dp),
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            icon = { HyperLogo(size = 64.dp) },
            title = { Text(stringResource(R.string.settings_about_dialog_title), fontWeight = FontWeight.ExtraBold) },
            text = {
                Column {
                    Text(stringResource(R.string.settings_about_dialog_body), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Text("v${BuildConfig.VERSION_NAME} · ${BuildConfig.VERSION_CODE}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(serverHost.baseUrl, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text(stringResource(R.string.close)) } },
            shape = RoundedCornerShape(28.dp),
        )
    }

    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            icon = { Icon(Icons.Rounded.Logout, contentDescription = null) },
            title = { Text(stringResource(R.string.logout_title)) },
            text = { Text(stringResource(R.string.logout_message)) },
            confirmButton = {
                TextButton(onClick = { showLogout = false; container.auth.logout() }) {
                    Text(stringResource(R.string.logout_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showLogout = false }) { Text(stringResource(R.string.cancel)) } },
            shape = RoundedCornerShape(28.dp),
        )
    }
}
