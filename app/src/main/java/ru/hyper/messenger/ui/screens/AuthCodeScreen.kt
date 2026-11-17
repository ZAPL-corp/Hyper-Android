package ru.hyper.messenger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hyper.messenger.R
import ru.hyper.messenger.data.AuthStep
import ru.hyper.messenger.data.NativeAuthViewModel
import ru.hyper.messenger.ui.components.HyperLogo
import ru.hyper.messenger.ui.components.SystemBarsForTheme

@Composable
fun EmailCodeScreen(
    viewModel: NativeAuthViewModel,
    onBack: () -> Unit,
    onTwoFactor: () -> Unit,
) {
    val state = viewModel.state
    LaunchedEffect(state.step) { if (state.step == AuthStep.TWO_FA) onTwoFactor() }
    CodeEntry(
        title = stringResource(R.string.email_code_title),
        subtitle = stringResource(R.string.email_code_subtitle, state.email),
        busy = state.busy,
        error = state.error,
        resendAvailable = state.resendAvailable,
        onBack = onBack,
        onSubmit = { code ->
            if (state.resendAvailable) viewModel.verifyEmail(code) else viewModel.submitEmailCode(code)
        },
        onResend = viewModel::resendVerification,
    )
}

@Composable
fun TwoFactorScreen(
    viewModel: NativeAuthViewModel,
    onBack: () -> Unit,
) {
    val state = viewModel.state
    CodeEntry(
        title = stringResource(R.string.twofa_headline),
        subtitle = stringResource(R.string.twofa_native_subtitle),
        busy = state.busy,
        error = state.error,
        resendAvailable = false,
        onBack = onBack,
        onSubmit = viewModel::submit2fa,
        onResend = {},
    )
}

@Composable
private fun CodeEntry(
    title: String,
    subtitle: String,
    busy: Boolean,
    error: String?,
    resendAvailable: Boolean,
    onBack: () -> Unit,
    onSubmit: (String) -> Unit,
    onResend: () -> Unit,
) {
    SystemBarsForTheme()
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Spacer(Modifier.weight(1f))
            HyperLogo(size = 28.dp)
        }
        Spacer(Modifier.height(26.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(26.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { code = it.filter(Char::isDigit).take(6) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.code_label)) },
            leadingIcon = { Icon(Icons.Rounded.Key, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                textAlign = TextAlign.Center, letterSpacing = 10.sp, fontWeight = FontWeight.SemiBold,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { if (code.length == 6) onSubmit(code) }),
            enabled = !busy,
            supportingText = { Text(stringResource(R.string.code_hint)) },
        )

        if (error != null) {
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
                    Icon(Icons.Rounded.ErrorOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { if (code.length == 6) onSubmit(code) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !busy && code.length == 6,
            shape = RoundedCornerShape(18.dp),
        ) {
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp), strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(stringResource(R.string.code_submit), style = MaterialTheme.typography.titleMedium)
            }
        }

        if (resendAvailable) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = onResend, enabled = !busy) {
                    Text(stringResource(R.string.code_resend))
                }
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}
