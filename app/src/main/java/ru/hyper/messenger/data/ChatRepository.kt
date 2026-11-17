package ru.hyper.messenger.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import ru.hyper.messenger.data.remote.Friend
import ru.hyper.messenger.data.remote.HyperApi
import ru.hyper.messenger.data.remote.RemoteGroup
import ru.hyper.messenger.data.remote.SocketIoClient
import ru.hyper.messenger.model.Account
import ru.hyper.messenger.model.Chat
import ru.hyper.messenger.model.ChatKind
import ru.hyper.messenger.model.Message

enum class SyncState { WAITING, SYNCING, READY }

class ChatRepository(
    private val preferences: PreferencesRepository,
    private val api: HyperApi,
) {
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val chatList = mutableStateListOf<Chat>()
    private val messagesByChat = mutableStateMapOf<String, SnapshotStateList<Message>>()
    private val friendChats = mutableStateListOf<Chat>()
    private val outbox = mutableListOf<Outgoing>()

    var isConnected = mutableStateOf(false)
        private set
    var isLoading = mutableStateOf(false)
        private set
    var syncState = mutableStateOf(SyncState.WAITING)
        private set
    var error = mutableStateOf<String?>(null)
        private set

    private var socket: SocketIoClient? = null
    private var account: Account? = null
    private var activeChatId: String? = null
    private var nextLocalId = -1L
    private var pendingHistory = 0

    fun chats(): List<Chat> = chatList
    fun friends(): List<Chat> = friendChats
    fun chatById(chatId: String): Chat? = chatList.firstOrNull { it.id == chatId }
    fun unreadTotal(): Int = chatList.sumOf { it.unreadCount }
    fun messagesFor(chatId: String): SnapshotStateList<Message> =
        messagesByChat.getOrPut(chatId) { mutableStateListOf() }

    fun setActiveChat(chatId: String?) { activeChatId = chatId }

    fun connect(next: Account?) {
        if (next == null) { teardown(true); return }
        if (account?.user?.id == next.user.id && socket != null) return
        teardown(true)
        account = next
        loadCache(next)
        loadOutbox(next)
        isLoading.value = true
        syncState.value = SyncState.WAITING

        ioScope.launch {
            val friends = api.friends(next.host, next.token)
            val groups = api.groups(next.host, next.token)
            mainScope.launch {
                applyChats(friends, groups)
                isLoading.value = false
                persist()
                startSocket(next)
            }
        }
    }

    fun disconnect() { teardown(false) }

    private fun teardown(clearData: Boolean) {
        socket?.disconnect()
        socket = null
        isConnected.value = false
        syncState.value = SyncState.WAITING
        if (clearData) {
            account = null
            activeChatId = null
            chatList.clear()
            messagesByChat.clear()
            friendChats.clear()
            outbox.clear()
        }
    }

    private fun applyChats(friends: List<Friend>, groups: List<RemoteGroup>) {
        val existing = chatList.associateBy { it.id }
        friendChats.clear()
        friends.forEach { friend ->
            val id = dmId(friend.username)
            val cached = existing[id]
            friendChats.add(
                Chat(
                    id = id,
                    title = friend.username,
                    kind = ChatKind.PERSONAL,
                    subtitle = "@${friend.username}",
                    avatarUrl = friend.customAvatar.takeIf { it.isNotBlank() },
                    lastMessage = cached?.lastMessage.orEmpty(),
                    time = cached?.time.orEmpty(),
                    unreadCount = cached?.unreadCount ?: 0,
                    pinned = cached?.pinned ?: false,
                ),
            )
        }
        val merged = mutableListOf<Chat>().apply { addAll(friendChats) }
        groups.forEach { group ->
            val id = groupId(group.id)
            val cached = existing[id]
            merged.add(
                Chat(
                    id = id,
                    title = group.name,
                    kind = ChatKind.GROUP,
                    subtitle = "Участников: ${group.members}",
                    lastMessage = cached?.lastMessage.orEmpty(),
                    time = cached?.time.orEmpty(),
                    unreadCount = cached?.unreadCount ?: 0,
                    pinned = cached?.pinned ?: false,
                ),
            )
        }
        chatList.clear()
        chatList.addAll(merged)
    }

    private fun startSocket(acc: Account) {
        val client = SocketIoClient(acc.host.baseUrl, acc.cookie)
        socket = client
        val me = acc.user.username.removePrefix("@")

        client.on("chat_history_response") { args ->
            val payload = args.optJSONObject(0) ?: return@on
            val withUser = payload.optString("withUser")
            val history = payload.optJSONArray("history") ?: return@on
            mainScope.launch {
                applyHistory(dmId(withUser), history, me)
                markHistoryReceived()
                persist()
            }
        }
        client.on("group_history_response") { args ->
            val payload = args.optJSONObject(0) ?: return@on
            val group = payload.optLong("groupId")
            val history = payload.optJSONArray("history") ?: return@on
            mainScope.launch {
                applyHistory(groupId(group), history, me)
                markHistoryReceived()
                persist()
            }
        }
        client.on("msg_receive") { args ->
            val payload = args.optJSONObject(0) ?: return@on
            val from = payload.optString("from")
            mainScope.launch {
                appendIncoming(dmId(from), from, payload.optLong("id"), payload.optString("message"))
                persist()
            }
        }
        client.on("group_msg_receive") { args ->
            val payload = args.optJSONObject(0) ?: return@on
            val from = payload.optString("from")
            mainScope.launch {
                appendIncoming(
                    groupId(payload.optLong("groupId")), from,
                    payload.optLong("id"), payload.optString("message"),
                )
                persist()
            }
        }
        client.on("msg_deleted") { args ->
            val id = args.optJSONObject(0)?.optLong("id") ?: return@on
            mainScope.launch {
                messagesByChat.values.forEach { it.removeAll { m -> m.id == id } }
                persist()
            }
        }
        client.on("auth_error") { args ->
            mainScope.launch {
                error.value = args.optJSONObject(0)?.optString("message").orEmpty()
                    .ifBlank { "Ошибка авторизации на сервере" }
            }
        }

        client.onOpen = {
            mainScope.launch {
                isConnected.value = true
                val targets = chatList.count { it.kind != ChatKind.CHANNEL }
                pendingHistory = targets
                syncState.value = if (targets == 0) SyncState.READY else SyncState.SYNCING
            }
            ioScope.launch {
                client.emit("identify", me)
                chatList.toList().forEach { chat ->
                    when (chat.kind) {
                        ChatKind.PERSONAL -> client.emit("get_chat_history", chat.title)
                        ChatKind.GROUP -> client.emit(
                            "get_group_history",
                            JSONObject().put("groupId", chat.id.substringAfter(':').toLongOrNull() ?: 0L),
                        )
                        else -> Unit
                    }
                }
                flushOutbox(client)
            }
        }
        client.onClosed = {
            mainScope.launch {
                isConnected.value = false
                syncState.value = SyncState.WAITING
            }
        }
        client.connect()
    }

    private fun markHistoryReceived() {
        pendingHistory = (pendingHistory - 1).coerceAtLeast(0)
        if (pendingHistory == 0) syncState.value = SyncState.READY
    }

    private fun applyHistory(chatId: String, history: JSONArray, me: String) {
        val list = messagesFor(chatId)
        val incoming = buildList {
            for (index in 0 until history.length()) {
                val item = history.optJSONObject(index) ?: continue
                val text = item.optString("message")
                if (text.isBlank()) continue
                val sender = item.optString("sender")
                add(
                    Message(
                        id = item.optLong("id"),
                        text = text,
                        time = formatTime(item.optString("created_at")),
                        isMine = sender == me,
                        sender = sender,
                    ),
                )
            }
        }
        list.clear()
        list.addAll(incoming)
        outbox.filter { it.chatId == chatId }.forEach { pending ->
            list.add(Message(pending.localId, pending.text, pending.time, isMine = true, pending = true))
        }
        list.lastOrNull()?.let { updateChat(chatId) { chat -> chat.copy(lastMessage = it.text, time = it.time) } }
    }

    private fun appendIncoming(chatId: String, sender: String, id: Long, text: String) {
        val list = messagesFor(chatId)
        if (id > 0 && list.any { it.id == id }) return
        val time = nowTime()
        list.add(Message(id, text, time, isMine = false, sender = sender))
        val unread = if (activeChatId == chatId) 0 else 1
        updateChat(chatId) { it.copy(lastMessage = text, time = time, unreadCount = it.unreadCount + unread) }
        if (chatId !in chatList.map { it.id }) {
            chatList.add(Chat(chatId, sender, text, time, unread, kind = ChatKind.PERSONAL))
        }
    }

    fun sendMessage(chatId: String, text: String, time: String) {
        val chat = chatById(chatId) ?: return
        val localId = nextLocalId--
        messagesFor(chatId).add(Message(localId, text, time, isMine = true, pending = true))
        updateChat(chatId) { it.copy(lastMessage = text, time = time, unreadCount = 0) }
        val payload = when (chat.kind) {
            ChatKind.PERSONAL -> JSONObject().put("to", chat.title).put("message", text)
            ChatKind.GROUP -> JSONObject()
                .put("groupId", chat.id.substringAfter(':').toLongOrNull() ?: 0L)
                .put("message", text)
            else -> null
        }
        val client = socket
        if (payload != null && client != null && client.connected.value) {
            client.emit(if (chat.kind == ChatKind.GROUP) "group_message" else "private_message", payload)
            markSent(chatId, localId)
        } else {
            outbox.add(Outgoing(chatId, text, time, localId))
            persistOutbox()
        }
        persist()
    }

    private fun flushOutbox(client: SocketIoClient) {
        val pending = outbox.toList()
        outbox.clear()
        persistOutbox()
        pending.forEach { outgoing ->
            val chat = chatById(outgoing.chatId) ?: return@forEach
            val payload = when (chat.kind) {
                ChatKind.PERSONAL -> JSONObject().put("to", chat.title).put("message", outgoing.text)
                ChatKind.GROUP -> JSONObject()
                    .put("groupId", chat.id.substringAfter(':').toLongOrNull() ?: 0L)
                    .put("message", outgoing.text)
                else -> null
            } ?: return@forEach
            client.emit(if (chat.kind == ChatKind.GROUP) "group_message" else "private_message", payload)
            mainScope.launch { markSent(outgoing.chatId, outgoing.localId) }
        }
    }

    private fun markSent(chatId: String, localId: Long) {
        val list = messagesByChat[chatId] ?: return
        val index = list.indexOfFirst { it.id == localId }
        if (index >= 0) list[index] = list[index].copy(pending = false)
    }

    fun markRead(chatId: String) { updateChat(chatId) { it.copy(unreadCount = 0) }; persist() }
    fun markUnread(chatId: String) { updateChat(chatId) { it.copy(unreadCount = maxOf(1, it.unreadCount)) }; persist() }
    fun togglePin(chatId: String) { updateChat(chatId) { it.copy(pinned = !it.pinned) }; persist() }

    fun deleteMessage(chatId: String, messageId: Long) {
        val chat = chatById(chatId) ?: return
        if (chat.kind == ChatKind.PERSONAL && messageId > 0) {
            socket?.emit("msg_delete", JSONObject().put("id", messageId).put("to", chat.title))
        }
        messagesByChat[chatId]?.removeAll { it.id == messageId }
        persist()
    }

    suspend fun createGroup(name: String, memberNames: List<String>, welcomeText: String, time: String): Chat? {
        val acc = account ?: return null
        val groupId = withContext(Dispatchers.IO) { api.createGroup(acc.host, acc.token, name, memberNames) } ?: return null
        val chat = Chat(
            id = groupId(groupId), title = name, kind = ChatKind.GROUP,
            subtitle = "Участников: ${memberNames.size + 1}",
            lastMessage = welcomeText, time = time,
        )
        mainScope.launch { chatList.add(chat); persist() }
        return chat
    }

    suspend fun findUser(query: String): Chat? {
        val acc = account ?: return null
        val clean = query.trim().removePrefix("@")
        if (clean.length < 2) return null
        val friend = withContext(Dispatchers.IO) { api.findUser(acc.host, acc.token, clean) } ?: return null
        return Chat(
            id = dmId(friend.username), title = friend.username, kind = ChatKind.PERSONAL,
            subtitle = "@${friend.username}",
            avatarUrl = friend.customAvatar.takeIf { it.isNotBlank() },
        )
    }

    fun ensureChat(chat: Chat): Chat {
        if (chatById(chat.id) == null) { chatList.add(chat); persist() }
        return chatById(chat.id) ?: chat
    }

    private fun updateChat(chatId: String, transform: (Chat) -> Chat) {
        val index = chatList.indexOfFirst { it.id == chatId }
        if (index >= 0) chatList[index] = transform(chatList[index])
    }

    private fun cacheKey(): String? = account?.user?.email?.let { "cache_$it" }

    private fun loadCache(acc: Account) {
        val raw = preferences.readCache("cache_${acc.user.email}") ?: return
        runCatching {
            val root = JSONObject(raw)
            val chats = root.optJSONArray("chats") ?: JSONArray()
            val messages = root.optJSONObject("messages") ?: JSONObject()
            chatList.clear()
            for (index in 0 until chats.length()) {
                val item = chats.optJSONObject(index) ?: continue
                chatList.add(
                    Chat(
                        id = item.optString("id"), title = item.optString("title"),
                        lastMessage = item.optString("last"), time = item.optString("time"),
                        unreadCount = item.optInt("unread"), pinned = item.optBoolean("pinned"),
                        kind = runCatching { ChatKind.valueOf(item.optString("kind")) }.getOrDefault(ChatKind.PERSONAL),
                        subtitle = item.optString("subtitle").takeIf { it.isNotBlank() },
                        avatarUrl = item.optString("avatar").takeIf { it.isNotBlank() },
                    ),
                )
            }
            messages.keys().forEach { chatId ->
                val array = messages.optJSONArray(chatId) ?: return@forEach
                val list = messagesFor(chatId)
                list.clear()
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    list.add(
                        Message(
                            id = item.optLong("id"), text = item.optString("text"),
                            time = item.optString("time"), isMine = item.optBoolean("mine"),
                            sender = item.optString("sender").takeIf { it.isNotBlank() },
                        ),
                    )
                }
            }
        }
    }

    private fun persist() {
        val key = cacheKey() ?: return
        val root = JSONObject()
        val chats = JSONArray()
        chatList.forEach { chat ->
            chats.put(
                JSONObject().put("id", chat.id).put("title", chat.title)
                    .put("last", chat.lastMessage).put("time", chat.time)
                    .put("unread", chat.unreadCount).put("pinned", chat.pinned)
                    .put("kind", chat.kind.name).put("subtitle", chat.subtitle.orEmpty())
                    .put("avatar", chat.avatarUrl.orEmpty()),
            )
        }
        val messages = JSONObject()
        messagesByChat.forEach { (chatId, list) ->
            val array = JSONArray()
            list.takeLast(100).forEach { message ->
                array.put(
                    JSONObject().put("id", message.id).put("text", message.text)
                        .put("time", message.time).put("mine", message.isMine)
                        .put("sender", message.sender.orEmpty()),
                )
            }
            messages.put(chatId, array)
        }
        root.put("chats", chats).put("messages", messages)
        preferences.writeCache(key, root.toString())
    }

    private fun loadOutbox(acc: Account) {
        outbox.clear()
        val raw = preferences.readCache("outbox_${acc.user.email}") ?: return
        runCatching {
            val array = JSONArray(raw)
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                outbox.add(
                    Outgoing(
                        item.optString("chat"), item.optString("text"),
                        item.optString("time"), item.optLong("local"),
                    ),
                )
            }
        }
    }

    private fun persistOutbox() {
        val email = account?.user?.email ?: return
        val array = JSONArray()
        outbox.forEach {
            array.put(
                JSONObject().put("chat", it.chatId).put("text", it.text)
                    .put("time", it.time).put("local", it.localId),
            )
        }
        preferences.writeCache("outbox_$email", array.toString())
    }

    private data class Outgoing(val chatId: String, val text: String, val time: String, val localId: Long)

    companion object {
        fun dmId(username: String): String = "dm:$username"
        fun groupId(id: Long): String = "group:$id"
        private fun nowTime(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        private fun formatTime(raw: String): String {
            if (raw.isBlank()) return nowTime()
            val parsed = runCatching {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(raw)
            }.getOrNull() ?: return raw.takeLast(5)
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(parsed)
        }
    }
}
