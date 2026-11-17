package ru.hyper.messenger.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.hyper.messenger.R
import ru.hyper.messenger.data.AuthRepository
import ru.hyper.messenger.ui.LocalAppContainer
import ru.hyper.messenger.ui.components.HyperAvatar
import ru.hyper.messenger.ui.components.SectionTitle
import ru.hyper.messenger.ui.components.SettingsDivider
import ru.hyper.messenger.ui.components.SettingsGroup
import ru.hyper.messenger.ui.components.SettingsRow

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onOpenThemes: () -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenAccounts: () -> Unit,
) {
    val container = LocalAppContainer.current
    val currentUser by container.auth.user.collectAsStateWithLifecycle()
    val accounts by container.auth.accountList.collectAsStateWithLifecycle()
    val user = currentUser ?: return
    var logout by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(top = 14.dp, bottom = 28.dp),
    ) {
        Text(stringResource(R.string.profile_title), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(14.dp))
        Surface(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HyperAvatar(name = user.displayName, size = 76.dp, imageUrl = user.customAvatar.takeIf { it.isNotBlank() })
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            user.displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            user.username,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.46f)) {
                    Text(
                        user.status.ifBlank { stringResource(R.string.profile_status_default) },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
                Spacer(Modifier.height(18.dp))
                Button(onClick = onEditProfile, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_edit))
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        SectionTitle(stringResource(R.string.profile_account))
        SettingsGroup {
            SettingsRow(Icons.Rounded.AlternateEmail, stringResource(R.string.profile_email), subtitle = user.email, onClick = null, showChevron = false)
            SettingsDivider()
            SettingsRow(Icons.Rounded.Badge, stringResource(R.string.profile_username), subtitle = user.username, onClick = null, showChevron = false)
        }

        Spacer(Modifier.height(18.dp))
        SectionTitle(stringResource(R.string.profile_actions))
        SettingsGroup {
            SettingsRow(Icons.Rounded.Palette, stringResource(R.string.profile_action_themes), subtitle = stringResource(R.string.profile_action_themes_desc), onClick = onOpenThemes)
            SettingsDivider()
            SettingsRow(Icons.Rounded.Security, stringResource(R.string.profile_action_security), subtitle = stringResource(R.string.profile_action_security_desc), onClick = onOpenSecurity)
            SettingsDivider()
            SettingsRow(
                Icons.Rounded.People,
                stringResource(R.string.profile_accounts),
                subtitle = stringResource(R.string.profile_accounts_value, accounts.size.coerceIn(1, AuthRepository.MAX_ACCOUNTS)),
                onClick = onOpenAccounts,
            )
        }

        Spacer(Modifier.height(18.dp))
        SettingsGroup {
            SettingsRow(
                Icons.Rounded.Logout, stringResource(R.string.profile_logout),
                onClick = { logout = true }, showChevron = false,
                iconBackground = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }

    if (logout) {
        AlertDialog(
            onDismissRequest = { logout = false },
            icon = { Icon(Icons.Rounded.Logout, contentDescription = null) },
            title = { Text(stringResource(R.string.logout_title)) },
            text = { Text(stringResource(R.string.logout_message)) },
            confirmButton = {
                TextButton(onClick = { logout = false; container.auth.logout() }) {
                    Text(stringResource(R.string.logout_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { logout = false }) { Text(stringResource(R.string.cancel)) } },
            shape = RoundedCornerShape(28.dp),
        )
    }
}
