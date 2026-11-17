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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.hyper.messenger.R
import ru.hyper.messenger.ui.LocalAppContainer
import ru.hyper.messenger.ui.components.HyperBackTopBar
import ru.hyper.messenger.ui.components.SystemBarsForTheme

@Composable
fun SecurityScreen(onBack: () -> Unit) {
    SystemBarsForTheme()
    val container = LocalAppContainer.current
    val account by container.auth.activeAccount.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val snackbar = remember { SnackbarHostState() }
    val enabled = account?.user?.twoFactorEnabled == true

    var setupSecret by remember { mutableStateOf<String?>(null) }
    var showEnable by remember { mutableStateOf(false) }
    var showDisable by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { HyperBackTopBar(stringResource(R.string.security_title), onBack) },
    ) { innerPadding ->
        Column(
            Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(stringResource(R.string.security_subtitle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            Surface(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (enabled) Icons.Rounded.Shield else Icons.Rounded.Security,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.security_twofa), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(if (enabled) R.string.security_enabled else R.string.security_twofa_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = {
                    if (busy) return@Button
                    if (enabled) showDisable = true else {
                        busy = true
                        scope.launch {
                            val setup = container.auth.setupTwoFactor()
                            busy = false
                            if (setup == null) snackbar.showSnackbar("Не удалось включить 2FA")
                            else { setupSecret = setup.secret; showEnable = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = !busy,
                shape = RoundedCornerShape(18.dp),
            ) {
                if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text(stringResource(if (enabled) R.string.security_disable else R.string.security_enable), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(16.dp))
            Surface(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    stringResource(R.string.security_server_note),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }

    if (showEnable && setupSecret != null) {
        TwoFactorDialog(
            title = stringResource(R.string.security_setup_title),
            body = stringResource(R.string.security_setup_body),
            secret = setupSecret,
            onCopy = {
                clipboard.setText(AnnotatedString(setupSecret.orEmpty()))
                scope.launch { snackbar.showSnackbar("Ключ скопирован") }
            },
            onDismiss = { showEnable = false },
            onConfirm = { code ->
                busy = true
                scope.launch {
                    val error = container.auth.setTwoFactor(code, true)
                    busy = false
                    if (error == null) { showEnable = false; snackbar.showSnackbar("2FA включена") }
                    else snackbar.showSnackbar(error)
                }
            },
            busy = busy,
        )
    }

    if (showDisable) {
        TwoFactorDialog(
            title = stringResource(R.string.security_disable_title),
            body = stringResource(R.string.security_disable_body),
            secret = null,
            onCopy = {},
            onDismiss = { showDisable = false },
            onConfirm = { code ->
                busy = true
                scope.launch {
                    val error = container.auth.setTwoFactor(code, false)
                    busy = false
                    if (error == null) { showDisable = false; snackbar.showSnackbar("2FA выключена") }
                    else snackbar.showSnackbar(error)
                }
            },
            busy = busy,
        )
    }
}

@Composable
private fun TwoFactorDialog(
    title: String,
    body: String,
    secret: String?,
    onCopy: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    busy: Boolean,
) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Key, contentDescription = null) },
        title = { Text(title) },
        text = {
            Column {
                Text(body, style = MaterialTheme.typography.bodyMedium)
                if (!secret.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.security_secret_label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(6.dp))
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Row(
                            Modifier.fillMaxWidth().clickable(onClick = onCopy).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(secret.chunked(4).joinToString(" "), style = MaterialTheme.typography.bodyMedium)
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.filter(Char::isDigit).take(6) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.security_code_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(code) }, enabled = code.length == 6 && !busy) {
                Text(stringResource(R.string.security_confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        shape = RoundedCornerShape(28.dp),
    )
}
