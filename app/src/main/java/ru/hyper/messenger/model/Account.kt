package ru.hyper.messenger.model

import ru.hyper.messenger.core.ServerHost

data class Account(
    val user: HyperUser,
    val token: String,
    val cookie: String,
    val hostKey: String = ServerHost.ANDROID.storageKey,
) {
    val host: ServerHost get() = ServerHost.fromKey(hostKey)
}
