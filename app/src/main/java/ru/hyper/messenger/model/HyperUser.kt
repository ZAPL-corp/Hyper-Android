package ru.hyper.messenger.model

data class HyperUser(
    val id: String,
    val displayName: String,
    val username: String,
    val email: String,
    val status: String,
    val isDemo: Boolean = false,
) {
    val initials: String
        get() = displayName
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifBlank { "H" }
}
