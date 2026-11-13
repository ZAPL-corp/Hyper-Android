package ru.hyper.messenger.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.hyper.messenger.R
import ru.hyper.messenger.data.DemoData
import ru.hyper.messenger.ui.LocalAppContainer
import ru.hyper.messenger.ui.components.HyperAvatar
import ru.hyper.messenger.ui.components.HyperBackTopBar
import ru.hyper.messenger.ui.components.SystemBarsForTheme

@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    SystemBarsForTheme()

    val container = LocalAppContainer.current
    val currentUser by container.auth.user.collectAsStateWithLifecycle()
    val user = currentUser ?: DemoData.demoUser

    var name by rememberSaveable(user.id) { mutableStateOf(user.displayName) }
    var status by rememberSaveable(user.id) { mutableStateOf(user.status) }
    var showNameError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val photoSoonMessage = stringResource(R.string.edit_profile_photo_soon)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HyperBackTopBar(
                title = stringResource(R.string.edit_profile_title),
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.edit_profile_headline),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(22.dp))

            HyperAvatar(
                name = name.ifBlank { user.displayName },
                size = 94.dp,
            )

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(photoSoonMessage)
                    }
                },
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.edit_profile_photo))
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (showNameError && it.isNotBlank()) showNameError = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.edit_profile_name)) },
                singleLine = true,
                isError = showNameError,
                supportingText = {
                    if (showNameError) {
                        Text(stringResource(R.string.edit_profile_name_empty))
                    }
                },
                shape = RoundedCornerShape(18.dp),
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = status,
                onValueChange = { status = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.edit_profile_status)) },
                supportingText = { Text(stringResource(R.string.edit_profile_status_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
            )

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.AlternateEmail,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.edit_profile_email_readonly),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        showNameError = true
                    } else {
                        val newStatus = status.ifBlank {
                            user.status.ifBlank { "На связи в HYPER" }
                        }
                        container.auth.updateProfile(name, newStatus)
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    text = stringResource(R.string.edit_profile_save),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
