package ru.hyper.messenger.data

import ru.hyper.messenger.data.remote.CookieJar

class AuthSession(var email: String = "") {
    val jar = CookieJar()
}

sealed interface AuthStepResult {
    data object Completed : AuthStepResult
    data object NeedEmailCode : AuthStepResult
    data object Need2fa : AuthStepResult
    data class Error(val message: String) : AuthStepResult
}
