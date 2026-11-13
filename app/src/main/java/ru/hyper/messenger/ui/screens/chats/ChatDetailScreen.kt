package ru.hyper.messenger.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import ru.hyper.messenger.R
import ru.hyper.messenger.data.DemoData
import ru.hyper.messenger.data.LocalChatStore
import ru.hyper.messenger.model.ChatKind
import ru.hyper.messenger.model.Message
import ru.hyper.messenger.ui.components.HyperAvatar
import ru.hyper.messenger.ui.components.SystemBarsForTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    onBack: () -> Unit,
) {
    SystemBarsForTheme()

    val chat = remember(chatId) { DemoData.chatById(chatId) }
    if (chat == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
            Text(
                text = stringResource(R.string.chat_not_found),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        return
    }

    val messages = LocalChatStore.messagesFor(chat.id)
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var draft by rememberSaveable(chat.id) { mutableStateOf("") }
    val featureSoonMessage = stringResource(R.string.demo_feature_soon)

    fun showFeatureSoon() {
        scope.launch {
            snackbarHostState.showSnackbar(featureSoonMessage)
        }
    }

    fun sendMessage() {
        val text = draft.trim()
        if (text.isEmpty()) return
        LocalChatStore.sendMessage(
            chatId = chat.id,
            text = text,
            time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
        )
        draft = ""
        scope.launch {
            listState.animateScrollToItem(messages.size + 1)
        }
    }

    LaunchedEffect(chat.id) {
        LocalChatStore.markRead(chat.id)
        listState.scrollToItem(messages.size + 1)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HyperAvatar(
                            name = chat.title,
                            size = 40.dp,
                            isGroup = chat.kind != ChatKind.PERSONAL,
                            online = chat.online,
                            seed = chat.id.hashCode(),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = chat.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                            )
                            Text(
                                text = when {
                                    chat.kind == ChatKind.GROUP -> stringResource(R.string.chat_group_label)
                                    chat.kind == ChatKind.CHANNEL -> stringResource(R.string.chat_channel_label)
                                    chat.online -> stringResource(R.string.chat_online)
                                    else -> stringResource(R.string.chat_last_seen_recently)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (chat.online) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showFeatureSoon() }) {
                        Icon(
                            imageVector = Icons.Rounded.Call,
                            contentDescription = stringResource(R.string.chat_call),
                        )
                    }
                    IconButton(onClick = { showFeatureSoon() }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = stringResource(R.string.chat_more),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            ChatComposer(
                draft = draft,
                onDraftChange = { draft = it },
                onSend = { sendMessage() },
                onAttach = { showFeatureSoon() },
                onVoice = { showFeatureSoon() },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            item(key = "date") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Text(
                            text = stringResource(R.string.today),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }
            }

            item(key = "demo-notice") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.chat_demo_notice),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            items(items = messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    showSender = !message.isMine && chat.kind == ChatKind.GROUP,
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    showSender: Boolean,
) {
    val isMine = message.isMine
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 322.dp),
        ) {
            if (showSender && message.sender != null) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 12.dp, bottom = 3.dp),
                )
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 21.dp,
                    topEnd = 21.dp,
                    bottomStart = if (isMine) 21.dp else 6.dp,
                    bottomEnd = if (isMine) 6.dp else 21.dp,
                ),
                color = if (isMine) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                contentColor = if (isMine) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = message.time,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isMine) {
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.68f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                        if (isMine) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.DoneAll,
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

@Composable
private fun ChatComposer(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit,
    onVoice: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            IconButton(
                onClick = onAttach,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Icon(
                    imageVector = Icons.Rounded.AttachFile,
                    contentDescription = stringResource(R.string.chat_attach),
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.width(8.dp))

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 46.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    if (draft.isEmpty()) {
                        Text(
                            text = stringResource(R.string.chat_input_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    BasicTextField(
                        value = draft,
                        onValueChange = onDraftChange,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = if (draft.isBlank()) onVoice else onSend),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (draft.isBlank()) {
                        Icons.Rounded.Mic
                    } else {
                        Icons.AutoMirrored.Rounded.Send
                    },
                    contentDescription = stringResource(
                        if (draft.isBlank()) R.string.chat_voice else R.string.chat_send,
                    ),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}
