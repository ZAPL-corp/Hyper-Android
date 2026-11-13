package ru.hyper.messenger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ru.hyper.messenger.R
import ru.hyper.messenger.data.AuthError
import ru.hyper.messenger.data.AuthUiState
import ru.hyper.messenger.data.AuthViewModel
import ru.hyper.messenger.data.DemoData
import ru.hyper.messenger.ui.components.HyperLogo
import ru.hyper.messenger.ui.components.SystemBarsForTheme

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onRegister: () -> Unit,
    onRequiresTwoFactor: (String) -> Unit,
) {
    SystemBarsForTheme()

    val state: AuthUiState = viewModel.uiState
    var login by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    fun submit() {
        focusManager.clearFocus()
        viewModel.login(login, password)
    }

    LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    LaunchedEffect(state.twoFactorLogin) {
        state.twoFactorLogin?.let { pendingLogin ->
            onRequiresTwoFactor(pendingLogin)
            viewModel.consumeTwoFactorLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.brand_name),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(6.dp))
                HyperLogo(size = 28.dp)
            }

            Spacer(Modifier.height(30.dp))

            HyperLogo(size = 66.dp)

            Spacer(Modifier.height(22.dp))

            Text(
                text = stringResource(R.string.login_headline),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = login,
                onValueChange = {
                    login = it
                    if (state.error != null) viewModel.clearError()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.login_email_label)) },
                leadingIcon = {
                    Icon(Icons.Rounded.AlternateEmail, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                enabled = !state.isLoading,
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (state.error != null) viewModel.clearError()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.login_password_label)) },
                leadingIcon = {
                    Icon(Icons.Rounded.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Rounded.VisibilityOff
                            } else {
                                Icons.Rounded.Visibility
                            },
                            contentDescription = stringResource(
                                if (passwordVisible) {
                                    R.string.login_hide_password
                                } else {
                                    R.string.login_show_password
                                },
                            ),
                        )
                    }
                },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                enabled = !state.isLoading,
            )

            if (state.error != null) {
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = state.error.authMessage(),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { submit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(18.dp),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.login_loading))
                } else {
                    Text(
                        text = stringResource(R.string.login_button),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.login_demo_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(R.string.login_demo_credentials),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.72f),
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    FilledTonalButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.login(DemoData.DEMO_EMAIL, DemoData.DEMO_PASSWORD)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.login_demo_button))
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.login_demo_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.68f),
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.login_no_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onRegister) {
                    Text(
                        text = stringResource(R.string.login_register),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AuthError.authMessage(): String = stringResource(
    when (this) {
        AuthError.EMPTY_LOGIN -> R.string.login_error_empty_login
        AuthError.EMPTY_PASSWORD -> R.string.login_error_empty_password
        AuthError.INVALID_LOGIN -> R.string.login_error_invalid
        AuthError.WRONG_DEMO_PASSWORD -> R.string.login_error_demo_password
        AuthError.EMPTY_CODE -> R.string.twofa_error_empty
        AuthError.INVALID_CODE -> R.string.twofa_error_invalid
    },
)
