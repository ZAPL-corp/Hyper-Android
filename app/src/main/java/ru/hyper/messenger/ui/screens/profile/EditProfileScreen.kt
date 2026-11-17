package ru.hyper.messenger.ui.screens.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hyper.messenger.R
import ru.hyper.messenger.ui.LocalAppContainer
import ru.hyper.messenger.ui.components.HyperAvatar
import ru.hyper.messenger.ui.components.HyperBackTopBar
import ru.hyper.messenger.ui.components.SystemBarsForTheme

@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    SystemBarsForTheme()
    val auth = LocalAppContainer.current.auth
    val currentUser by auth.user.collectAsStateWithLifecycle()
    val user = currentUser ?: return
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var username by rememberSaveable(user.id) { mutableStateOf(user.username.removePrefix("@")) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf(false) }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                uploading = true
                val data = withContext(Dispatchers.IO) { encodeAvatar(context, uri) }
                if (data != null) auth.uploadAvatar(data)
                uploading = false
            }
        }
    }
    val requestMedia = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) pickImage.launch("image/*")
    }

    fun pickAvatar() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        val granted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (granted) pickImage.launch("image/*") else requestMedia.launch(permissions)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { HyperBackTopBar(stringResource(R.string.edit_profile_title), onBack) },
    ) { innerPadding ->
        Column(
            Modifier.fillMaxSize().padding(innerPadding).imePadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.edit_profile_headline),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(22.dp))
            HyperAvatar(
                name = username.ifBlank { user.displayName },
                size = 94.dp,
                imageUrl = user.customAvatar.takeIf { it.isNotBlank() },
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { pickAvatar() }, enabled = !uploading, shape = RoundedCornerShape(14.dp)) {
                if (uploading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.edit_profile_photo))
                }
            }
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { input ->
                    username = input.trimStart().removePrefix("@")
                        .filter { it.isLetterOrDigit() || it == '_' || it == '.' || it == '-' }.take(32)
                    error = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.edit_profile_username)) },
                leadingIcon = { Icon(Icons.Rounded.Badge, contentDescription = null) },
                prefix = { Text("@") },
                singleLine = true,
                isError = error != null,
                supportingText = { Text(error ?: stringResource(R.string.edit_profile_username_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                shape = RoundedCornerShape(18.dp),
            )
            Spacer(Modifier.height(12.dp))
            Surface(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Column(Modifier.padding(16.dp)) {
                    Icon(Icons.Rounded.AlternateEmail, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(user.email, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.edit_profile_email_readonly),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (saving) return@Button
                    saving = true
                    scope.launch {
                        val result = auth.updateUsername(username)
                        saving = false
                        if (result == null) onBack() else error = result
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = !saving,
                shape = RoundedCornerShape(18.dp),
            ) {
                if (saving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.edit_profile_save), style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

private fun encodeAvatar(context: Context, uri: Uri): String? {
    val input = context.contentResolver.openInputStream(uri) ?: return null
    val bitmap = BitmapFactory.decodeStream(input) ?: return null
    input.close()
    val max = 512
    val scale = minOf(1f, max.toFloat() / maxOf(bitmap.width, bitmap.height))
    val scaled = if (scale < 1f) {
        Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
    } else bitmap
    val output = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, 80, output)
    val bytes = output.toByteArray()
    if (bytes.size > 3_000_000) return null
    return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
}
