package ru.hyper.messenger.model

enum class ChatKind {
    PERSONAL,
    GROUP,
    CHANNEL,
}

data class Chat(
    val id: String,
    val title: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val pinned: Boolean = false,
    val online: Boolean = false,
    val kind: ChatKind = ChatKind.PERSONAL,
)
