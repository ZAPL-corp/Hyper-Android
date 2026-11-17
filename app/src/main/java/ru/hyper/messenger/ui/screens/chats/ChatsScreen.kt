package ru.hyper.messenger.ui.screens.chats

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.MarkChatRead
import androidx.compose.material.icons.rounded.MarkChatUnread
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.hyper.messenger.R
import ru.hyper.messenger.data.SyncState
import ru.hyper.messenger.model.Chat
import ru.hyper.messenger.model.ChatKind
import ru.hyper.messenger.ui.LocalAppContainer
import ru.hyper.messenger.ui.components.EmptyState
import ru.hyper.messenger.ui.components.HyperAvatar
import ru.hyper.messenger.ui.components.SectionTitle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatsScreen(
    userName: String,
    onOpenChat: (String) -> Unit,
    onOpenProfile: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var showNewChat by rememberSaveable { mutableStateOf(false) }
    var showGroup by rememberSaveable { mutableStateOf(false) }
    var actionChatId by rememberSaveable { mutableStateOf<String?>(null) }
    var foundUser by remember { mutableStateOf<Chat?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val repository = LocalAppContainer.current.chats
    val context = androidx.compose.ui.platform.LocalContext.current

    val allChats = repository.chats()
    val contacts = repository.friends()
    val sync = repository.syncState.value
    val visible = allChats.filter {
        query.isBlank() || it.title.contains(query, true) || it.lastMessage.contains(query, true)
    }
    val pinned = visible.filter { it.pinned }
    val rest = visible.filterNot { it.pinned }
    val actionChat = allChats.firstOrNull { it.id == actionChatId }

    LaunchedEffect(query) {
        foundUser = null
        if (query.trim().removePrefix("@").length >= 2) {
            delay(450)
            foundUser = repository.findUser(query)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 18.dp, top = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = when (sync) {
                            SyncState.WAITING -> stringResource(R.string.chats_waiting)
                            SyncState.SYNCING -> stringResource(R.string.chats_syncing)
                            SyncState.READY -> stringResource(R.string.chats_title)
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = stringResource(R.string.chats_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onOpenProfile) { HyperAvatar(name = userName, size = 44.dp) }
            }

            Surface(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(21.dp))
                    Spacer(Modifier.width(12.dp))
                    Box(Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.chats_search_hint),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            foundUser?.let { user ->
                if (allChats.none { it.id == user.id }) {
                    SectionTitle(stringResource(R.string.search_global), Modifier.padding(start = 8.dp, top = 4.dp))
                    ChatRow(user, onClick = {
                        val chat = repository.ensureChat(user)
                        onOpenChat(chat.id)
                    }, onLongClick = {})
                }
            }

            if (visible.isEmpty() && foundUser == null) {
                Box(Modifier.fillMaxSize().padding(bottom = 90.dp), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Rounded.SearchOff,
                        title = stringResource(R.string.chats_empty_title),
                        description = stringResource(R.string.chats_empty_desc),
                    )
                }
            } else {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp, 0.dp, 12.dp, 104.dp)) {
                    if (pinned.isNotEmpty()) {
                        item("pinned") { SectionTitle(stringResource(R.string.chats_section_pinned), Modifier.padding(start = 8.dp, top = 4.dp)) }
                        items(pinned, key = { it.id }) { chat ->
                            ChatRow(chat, { onOpenChat(chat.id) }, { actionChatId = chat.id })
                        }
                    }
                    if (rest.isNotEmpty()) {
                        item("all") {
                            SectionTitle(
                                stringResource(R.string.chats_section_all),
                                Modifier.padding(start = 8.dp, top = if (pinned.isEmpty()) 4.dp else 14.dp),
                            )
                        }
                        items(rest, key = { it.id }) { chat ->
                            ChatRow(chat, { onOpenChat(chat.id) }, { actionChatId = chat.id })
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showNewChat = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 18.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(20.dp),
            icon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
            text = { Text(stringResource(R.string.new_chat), fontWeight = FontWeight.SemiBold) },
        )
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 92.dp))
    }

    if (showNewChat) {
        ModalBottomSheet(
            onDismissRequest = { showNewChat = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(22.dp)) {
                Text(stringResource(R.string.new_chat_title), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.new_chat_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                if (contacts.isEmpty()) {
                    Text(
                        stringResource(R.string.chats_no_friends),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
                contacts.forEach { chat ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                            .combinedClickable(onClick = { showNewChat = false; onOpenChat(chat.id) }, onLongClick = null)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HyperAvatar(chat.title, size = 46.dp, imageUrl = chat.avatarUrl)
                        Spacer(Modifier.width(12.dp))
                        Text(chat.title, style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .combinedClickable(onClick = { showNewChat = false; showGroup = true }, onLongClick = null)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(46.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(stringResource(R.string.new_chat_group), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.new_chat_group_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    if (actionChat != null) {
        ModalBottomSheet(
            onDismissRequest = { actionChatId = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp)) {
                Text(actionChat.title, style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(10.dp))
                ActionRow(
                    icon = Icons.Rounded.PushPin,
                    label = stringResource(if (actionChat.pinned) R.string.chat_action_unpin else R.string.chat_action_pin),
                ) { repository.togglePin(actionChat.id); actionChatId = null }
                ActionRow(
                    icon = if (actionChat.unreadCount > 0) Icons.Rounded.MarkChatRead else Icons.Rounded.MarkChatUnread,
                    label = stringResource(
                        if (actionChat.unreadCount > 0) R.string.chat_action_mark_read else R.string.chat_action_mark_unread,
                    ),
                ) {
                    if (actionChat.unreadCount > 0) repository.markRead(actionChat.id) else repository.markUnread(actionChat.id)
                    actionChatId = null
                }
            }
        }
    }

    if (showGroup) {
        CreateGroupDialog(
            contacts = contacts,
            onDismiss = { showGroup = false },
            onCreate = { name, members ->
                scope.launch {
                    val chat = repository.createGroup(
                        name, members,
                        context.getString(R.string.group_system_created, name),
                        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                    )
                    if (chat != null) { showGroup = false; onOpenChat(chat.id) }
                    else snackbar.showSnackbar(context.getString(R.string.group_create_error))
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick, onLongClick = null)
            .padding(8.dp, 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CreateGroupDialog(contacts: List<Chat>, onDismiss: () -> Unit, onCreate: (String, List<String>) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }
    val selected = remember { mutableStateMapOf<String, Boolean>() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.group_create_title), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; if (it.isNotBlank()) error = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.group_name_label)) },
                    placeholder = { Text(stringResource(R.string.group_name_hint)) },
                    singleLine = true,
                    isError = error,
                    supportingText = { if (error) Text(stringResource(R.string.group_name_required)) },
                    shape = RoundedCornerShape(16.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.group_members_label), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                contacts.filter { it.kind != ChatKind.GROUP }.forEach { contact ->
                    val checked = selected[contact.title] == true
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .combinedClickable(onClick = { selected[contact.title] = !checked }, onLongClick = null)
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HyperAvatar(contact.title, size = 36.dp, imageUrl = contact.avatarUrl)
                        Spacer(Modifier.width(10.dp))
                        Text(contact.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Box(
                            Modifier.size(24.dp).clip(CircleShape).background(
                                if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (checked) Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) error = true
                else onCreate(name.trim(), selected.filterValues { it }.keys.toList())
            }, shape = RoundedCornerShape(14.dp)) { Text(stringResource(R.string.group_create)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        shape = RoundedCornerShape(28.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatRow(chat: Chat, onClick: () -> Unit, onLongClick: () -> Unit) {
    val unread = chat.unreadCount > 0
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .background(if (unread) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HyperAvatar(
            name = chat.title,
            size = 55.dp,
            isGroup = chat.kind != ChatKind.PERSONAL,
            online = chat.online,
            seed = chat.id.hashCode(),
            imageUrl = chat.avatarUrl,
        )
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    chat.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    chat.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (unread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    chat.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (unread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (unread) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier.size(21.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else if (chat.pinned) {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Rounded.PushPin, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}
