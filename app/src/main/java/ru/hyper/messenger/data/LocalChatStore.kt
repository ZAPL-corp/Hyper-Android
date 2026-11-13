package ru.hyper.messenger.data

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import ru.hyper.messenger.model.Chat
import ru.hyper.messenger.model.Message

/**
 * Session-scoped chat state for the concept. Sent messages and read state survive navigation
 * while the app is running, but are intentionally not persisted across restarts yet.
 */
object LocalChatStore {
    private data class ChatPreview(
        val lastMessage: String,
        val time: String,
    )

    private val messagesByChat = mutableStateMapOf<String, SnapshotStateList<Message>>().apply {
        DemoData.chats.forEach { chat ->
            put(chat.id, mutableStateListOf<Message>().apply { addAll(DemoData.messagesFor(chat.id)) })
        }
    }
    private val previews = mutableStateMapOf<String, ChatPreview>()
    private val readChats = mutableStateMapOf<String, Boolean>()
    private var nextMessageId = 10_000L

    fun chats(): List<Chat> = DemoData.chats.map { chat ->
        val preview = previews[chat.id]
        chat.copy(
            lastMessage = preview?.lastMessage ?: chat.lastMessage,
            time = preview?.time ?: chat.time,
            unreadCount = if (readChats[chat.id] == true) 0 else chat.unreadCount,
        )
    }

    fun unreadTotal(): Int = chats().sumOf { it.unreadCount }

    fun messagesFor(chatId: String): SnapshotStateList<Message> =
        messagesByChat.getOrPut(chatId) {
            mutableStateListOf<Message>().apply { addAll(DemoData.messagesFor(chatId)) }
        }

    fun sendMessage(chatId: String, text: String, time: String) {
        val messages = messagesByChat[chatId] ?: return
        messages.add(
            Message(
                id = nextMessageId++,
                text = text,
                time = time,
                isMine = true,
            ),
        )
        previews[chatId] = ChatPreview(lastMessage = text, time = time)
        readChats[chatId] = true
    }

    fun markRead(chatId: String) {
        readChats[chatId] = true
    }
}
