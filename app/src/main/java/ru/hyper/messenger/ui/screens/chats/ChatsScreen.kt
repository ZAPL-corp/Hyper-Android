package ru.hyper.messenger.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.hyper.messenger.R
import ru.hyper.messenger.data.DemoData
import ru.hyper.messenger.data.LocalChatStore
import ru.hyper.messenger.model.Chat
import ru.hyper.messenger.model.ChatKind
import ru.hyper.messenger.ui.components.DemoBadge
import ru.hyper.messenger.ui.components.EmptyState
import ru.hyper.messenger.ui.components.HyperAvatar
import ru.hyper.messenger.ui.components.SectionTitle

private enum class ChatFilter {
    ALL,
    UNREAD,
    PERSONAL,
    GROUPS,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    userName: String,
    onOpenChat: (String) -> Unit,
    onOpenProfile: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(ChatFilter.ALL) }
    var showNewChatSheet by rememberSaveable { mutableStateOf(false) }

    val allChats = LocalChatStore.chats()
    val visibleChats = allChats.filter { chat ->
        val matchesQuery = query.isBlank() ||
            chat.title.contains(query, ignoreCase = true) ||
            chat.lastMessage.contains(query, ignoreCase = true)
        val matchesFilter = when (filter) {
            ChatFilter.ALL -> true
            ChatFilter.UNREAD -> chat.unreadCount > 0
            ChatFilter.PERSONAL -> chat.kind == ChatKind.PERSONAL
            ChatFilter.GROUPS -> chat.kind != ChatKind.PERSONAL
        }
        matchesQuery && matchesFilter
    }
    val pinned = visibleChats.filter { it.pinned }
    val rest = visibleChats.filterNot { it.pinned }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 18.dp, top = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.chats_title),
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
                IconButton(onClick = onOpenProfile) {
                    HyperAvatar(
                        name = userName,
                        size = 44.dp,
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(21.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
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
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { query = "" },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.close),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ChatFilter.entries.forEach { item ->
                    val label = when (item) {
                        ChatFilter.ALL -> R.string.chat_filter_all
                        ChatFilter.UNREAD -> R.string.chat_filter_unread
                        ChatFilter.PERSONAL -> R.string.chat_filter_personal
                        ChatFilter.GROUPS -> R.string.chat_filter_groups
                    }
                    FilterChip(
                        selected = filter == item,
                        onClick = { filter = item },
                        label = { Text(stringResource(label)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }

            if (visibleChats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 90.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        icon = Icons.Rounded.SearchOff,
                        title = stringResource(R.string.chats_empty_title),
                        description = stringResource(R.string.chats_empty_desc),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 0.dp,
                        bottom = 104.dp,
                    ),
                ) {
                    if (pinned.isNotEmpty()) {
                        item(key = "pinned-header") {
                            SectionTitle(
                                text = stringResource(R.string.chats_section_pinned),
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                            )
                        }
                        items(items = pinned, key = { it.id }) { chat ->
                            ChatRow(chat = chat, onClick = { onOpenChat(chat.id) })
                        }
                    }

                    if (rest.isNotEmpty()) {
                        item(key = "all-header") {
                            SectionTitle(
                                text = stringResource(R.string.chats_section_all),
                                modifier = Modifier.padding(start = 8.dp, top = if (pinned.isEmpty()) 4.dp else 14.dp),
                            )
                        }
                        items(items = rest, key = { it.id }) { chat ->
                            ChatRow(chat = chat, onClick = { onOpenChat(chat.id) })
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showNewChatSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 18.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.new_chat),
                    fontWeight = FontWeight.SemiBold,
                )
            },
        )
    }

    if (showNewChatSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNewChatSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 22.dp, end = 22.dp, bottom = 24.dp),
            ) {
                Text(
                    text = stringResource(R.string.new_chat_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.new_chat_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(18.dp))

                DemoData.suggestedContacts().forEach { chat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                showNewChatSheet = false
                                onOpenChat(chat.id)
                            }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HyperAvatar(
                            name = chat.title,
                            size = 46.dp,
                            isGroup = chat.kind == ChatKind.GROUP,
                            online = chat.online,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = chat.title,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = if (chat.kind == ChatKind.GROUP) {
                                    stringResource(R.string.chat_filter_groups)
                                } else {
                                    if (chat.online) {
                                        stringResource(R.string.chat_online)
                                    } else {
                                        stringResource(R.string.chat_last_seen_recently)
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.new_chat_group),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.new_chat_group_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DemoBadge(text = stringResource(R.string.soon))
                }
            }
        }
    }
}

@Composable
private fun ChatRow(
    chat: Chat,
    onClick: () -> Unit,
) {
    val unread = chat.unreadCount > 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .background(
                if (unread) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f)
                } else {
                    androidx.compose.ui.graphics.Color.Transparent
                },
            )
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HyperAvatar(
            name = chat.title,
            size = 55.dp,
            isGroup = chat.kind != ChatKind.PERSONAL,
            online = chat.online,
            seed = chat.id.hashCode(),
        )

        Spacer(Modifier.width(13.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (chat.kind == ChatKind.CHANNEL) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Rounded.Groups,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = chat.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (unread) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            Spacer(Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (unread) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(21.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else if (chat.pinned) {
                    Icon(
                        imageVector = Icons.Rounded.PushPin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
    }
}
