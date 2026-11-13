package ru.hyper.messenger.model

data class Message(
    val id: Long,
    val text: String,
    val time: String,
    val isMine: Boolean = false,
    val sender: String? = null,
)
