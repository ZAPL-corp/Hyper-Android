package ru.hyper.messenger.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
fun PrivacyScreen(onBack: () -> Unit) {
    SystemBarsForTheme()

    val prefs = LocalAppContainer.current.preferences
    val readReceiptsEnabled by prefs.readReceiptsEnabled.collectAsStateWithLifecycle()
    val lastSeenEnabled by prefs.lastSeenEnabled.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HyperBackTopBar(
                title = stringResource(R.string.privacy_title),
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
                text = stringResource(R.string.privacy_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))

            SettingsGroup {
                SettingsRow(
                    icon = Icons.Rounded.DoneAll,
                    title = stringResource(R.string.privacy_read_receipts),
                    subtitle = stringResource(R.string.privacy_read_receipts_desc),
                    onClick = { prefs.setReadReceiptsEnabled(!readReceiptsEnabled) },
                    trailing = {
                        Switch(
                            checked = readReceiptsEnabled,
                            onCheckedChange = prefs::setReadReceiptsEnabled,
                        )
                    },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Visibility,
                    title = stringResource(R.string.privacy_last_seen),
                    subtitle = stringResource(R.string.privacy_last_seen_desc),
                    onClick = { prefs.setLastSeenEnabled(!lastSeenEnabled) },
                    trailing = {
                        Switch(
                            checked = lastSeenEnabled,
                            onCheckedChange = prefs::setLastSeenEnabled,
                        )
                    },
                )
            }

            Spacer(Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Text(
                    text = stringResource(R.string.privacy_note),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                )
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
