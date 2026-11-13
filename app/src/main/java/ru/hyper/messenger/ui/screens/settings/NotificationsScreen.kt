package ru.hyper.messenger.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.hyper.messenger.R
import ru.hyper.messenger.ui.LocalAppContainer
import ru.hyper.messenger.ui.components.HyperBackTopBar
import ru.hyper.messenger.ui.components.SettingsDivider
import ru.hyper.messenger.ui.components.SettingsGroup
import ru.hyper.messenger.ui.components.SettingsRow
import ru.hyper.messenger.ui.components.SystemBarsForTheme

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    SystemBarsForTheme()

    val prefs = LocalAppContainer.current.preferences
    val notificationsEnabled by prefs.notificationsEnabled.collectAsStateWithLifecycle()
    val soundsEnabled by prefs.soundsEnabled.collectAsStateWithLifecycle()
    val previewEnabled by prefs.messagePreviewEnabled.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HyperBackTopBar(
                title = stringResource(R.string.notifications_title),
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.notifications_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))

            SettingsGroup {
                SettingsRow(
                    icon = Icons.Rounded.NotificationsActive,
                    title = stringResource(R.string.notifications_messages),
                    subtitle = stringResource(R.string.notifications_messages_desc),
                    onClick = { prefs.setNotificationsEnabled(!notificationsEnabled) },
                    trailing = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = prefs::setNotificationsEnabled,
                        )
                    },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.VolumeUp,
                    title = stringResource(R.string.notifications_sounds),
                    subtitle = stringResource(R.string.notifications_sounds_desc),
                    onClick = {
                        if (notificationsEnabled) prefs.setSoundsEnabled(!soundsEnabled)
                    },
                    trailing = {
                        Switch(
                            checked = soundsEnabled,
                            onCheckedChange = prefs::setSoundsEnabled,
                            enabled = notificationsEnabled,
                        )
                    },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Message,
                    title = stringResource(R.string.notifications_preview),
                    subtitle = stringResource(R.string.notifications_preview_desc),
                    onClick = {
                        if (notificationsEnabled) prefs.setMessagePreviewEnabled(!previewEnabled)
                    },
                    trailing = {
                        Switch(
                            checked = previewEnabled,
                            onCheckedChange = prefs::setMessagePreviewEnabled,
                            enabled = notificationsEnabled,
                        )
                    },
                )
            }

            if (!notificationsEnabled) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.notifications_master_off),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
