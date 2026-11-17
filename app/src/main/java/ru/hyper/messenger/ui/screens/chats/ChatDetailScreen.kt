package ru.hyper.messenger.ui.screens.chats

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import ru.hyper.messenger.R
import ru.hyper.messenger.model.ChatKind
import ru.hyper.messenger.model.Message
import ru.hyper.messenger.ui.LocalAppContainer
import ru.hyper.messenger.ui.components.HyperAvatar
import ru.hyper.messenger.ui.components.SystemBarsForTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatDetailScreen(chatId: String, onBack: () -> Unit) {
    SystemBarsForTheme()
    val repository = LocalAppContainer.current.chats
    val chat = repository.chatById(chatId)
    if (chat == null) {
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(stringResource(R.string.chat_not_found), style = MaterialTheme.typography.headlineSmall)
        }
        return
    }

    val messages = repository.messagesFor(chat.id)
    val listState = rememberLazyListState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var draft by rememberSaveable(chat.id) { mutableStateOf("") }
    var menuMessage by remember { mutableStateOf<Message?>(null) }
    var showAttach by remember { mutableStateOf(false) }

    fun sendPicked(uri: Uri) {
        repository.sendMessage(
            chat.id,
            "Файл: " + context.displayNameOf(uri),
            LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
        )
    }

    val galleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) sendPicked(it)
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) sendPicked(it)
    }
    val mediaPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) galleryPicker.launch("image/*")
    }

    fun openGallery() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        val granted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (granted) galleryPicker.launch("image/*") else mediaPermission.launch(permissions)
    }

    fun openFiles() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            mediaPermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        } else {
            filePicker.launch(arrayOf("*/*"))
        }
    }

    fun sendMessage() {
        val text = draft.trim()
        if (text.isEmpty()) return
        repository.sendMessage(chat.id, text, LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
        draft = ""
        scope.launch { listState.animateScrollToItem(messages.size.coerceAtLeast(0)) }
    }

    LaunchedEffect(chat.id) {
        repository.setActiveChat(chat.id)
        repository.markRead(chat.id)
        if (messages.isNotEmpty()) listState.scrollToItem(messages.size)
    }
    DisposableEffect(chat.id) { onDispose { repository.setActiveChat(null) } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HyperAvatar(
                            name = chat.title, size = 40.dp,
                            isGroup = chat.kind != ChatKind.PERSONAL,
                            seed = chat.id.hashCode(), imageUrl = chat.avatarUrl,
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(chat.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                            Text(
                                when (chat.kind) {
                                    ChatKind.GROUP -> stringResource(R.string.chat_group_label)
                                    else -> chat.subtitle ?: stringResource(R.string.chat_online)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            ChatComposer(draft, { draft = it }, { sendMessage() }, { showAttach = true })
        },
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            item("date") {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.Center) {
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Text(
                            stringResource(R.string.today),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }
            }
            if (messages.isEmpty()) {
                item("empty") {
                    Text(
                        stringResource(R.string.chat_start),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    )
                }
            }
            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    showSender = !message.isMine && chat.kind == ChatKind.GROUP,
                    onLongClick = { menuMessage = message },
                    modifier = Modifier.animateItem(fadeInSpec = tween(180), placementSpec = tween(220)),
                )
            }
        }
    }

    if (showAttach) {
        ModalBottomSheet(
            onDismissRequest = { showAttach = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp)) {
                Text(stringResource(R.string.attach_title), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(10.dp))
                SheetAction(Icons.Rounded.PhotoLibrary, stringResource(R.string.attach_gallery)) {
                    showAttach = false; openGallery()
                }
                Text(
                    stringResource(R.string.attach_gallery_desc),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 44.dp, bottom = 10.dp),
                )
                SheetAction(Icons.Rounded.Folder, stringResource(R.string.attach_files)) {
                    showAttach = false; openFiles()
                }
                Text(
                    stringResource(R.string.attach_files_desc),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 44.dp, bottom = 8.dp),
                )
            }
        }
    }

    menuMessage?.let { message ->
        val canDelete = message.isMine && chat.kind == ChatKind.PERSONAL && message.id > 0
        ModalBottomSheet(
            onDismissRequest = { menuMessage = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp)) {
                Text(message.text, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                Spacer(Modifier.height(10.dp))
                SheetAction(Icons.Rounded.ContentCopy, stringResource(R.string.message_action_copy)) {
                    clipboard.setText(AnnotatedString(message.text))
                    menuMessage = null
                    scope.launch { snackbar.showSnackbar(context.getString(R.string.message_copied)) }
                }
                if (canDelete) {
                    SheetAction(Icons.Rounded.DeleteOutline, stringResource(R.string.message_action_delete), destructive = true) {
                        repository.deleteMessage(chat.id, message.id)
                        menuMessage = null
                        scope.launch { snackbar.showSnackbar(context.getString(R.string.message_deleted)) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SheetAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick, onLongClick = null)
            .padding(horizontal = 8.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon, contentDescription = null,
            tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(14.dp))
        Text(
            label, style = MaterialTheme.typography.titleMedium,
            color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: Message,
    showSender: Boolean,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mine = message.isMine
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (mine) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 322.dp),
        ) {
            if (showSender && message.sender != null) {
                Text(
                    message.sender,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 12.dp, bottom = 3.dp),
                )
            }
            Surface(
                shape = RoundedCornerShape(
                    topStart = 21.dp, topEnd = 21.dp,
                    bottomStart = if (mine) 21.dp else 6.dp,
                    bottomEnd = if (mine) 6.dp else 21.dp,
                ),
                color = if (mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = if (mine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.combinedClickable(onClick = {}, onLongClick = onLongClick),
            ) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Text(message.text, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            message.time,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (mine) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.68f)
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (mine) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                if (message.pending) Icons.Rounded.Schedule else Icons.Rounded.DoneAll,
                                contentDescription = stringResource(R.string.message_read),
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.68f),
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatComposer(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth().imePadding().navigationBarsPadding(),
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 10.dp), verticalAlignment = Alignment.Bottom) {
            IconButton(
                onClick = onAttach,
                modifier = Modifier.size(46.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Icon(Icons.Rounded.AttachFile, contentDescription = stringResource(R.string.chat_attach), modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                modifier = Modifier.weight(1f).heightIn(min = 46.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    if (draft.isEmpty()) {
                        Text(
                            stringResource(R.string.chat_input_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    BasicTextField(
                        value = draft,
                        onValueChange = onDraftChange,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.size(46.dp).clip(CircleShape)
                    .background(if (draft.isBlank()) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.primary)
                    .combinedClickable(onClick = { if (draft.isNotBlank()) onSend() }, onLongClick = null),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Send,
                    contentDescription = stringResource(R.string.chat_send),
                    tint = if (draft.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

private fun Context.displayNameOf(uri: Uri): String {
    var name = uri.lastPathSegment?.substringAfterLast('/').orEmpty()
    runCatching {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index)?.let { name = it }
            }
        }
    }
    return name.ifBlank { "file" }
}
