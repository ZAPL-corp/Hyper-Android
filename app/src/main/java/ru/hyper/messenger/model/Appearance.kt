package ru.hyper.messenger.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class AccentPalette {
    HYPER,
    VIOLET,
    EMERALD,
    SUNSET,
    ROSE,
}

sealed interface AuthResult {
    data object Success : AuthResult
    data class RequiresTwoFactor(val login: String) : AuthResult
    data object WrongDemoPassword : AuthResult
    data object InvalidLogin : AuthResult
    data object InvalidCode : AuthResult
}
