package ru.hyper.messenger.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.hyper.messenger.core.ServerHost

enum class AuthStep { IDLE, EMAIL_CODE, TWO_FA, DONE }

data class NativeAuthState(
    val busy: Boolean = false,
    val error: String? = null,
    val step: AuthStep = AuthStep.IDLE,
    val email: String = "",
    val needsCaptcha: Boolean = false,
    val resendAvailable: Boolean = false,
)

class NativeAuthViewModel(private val auth: AuthRepository) : ViewModel() {
    var state by mutableStateOf(NativeAuthState())
        private set

    private var session = AuthSession()
    private var pending: Pending? = null
    private var host: ServerHost = ServerHost.ANDROID

    fun startLogin(email: String, password: String, host: ServerHost) {
        resetFlow()
        this.host = host
        pending = Pending.Login(email.trim(), password)
        state = state.copy(email = email.trim(), needsCaptcha = true)
    }

    fun startRegister(email: String, username: String, password: String, host: ServerHost) {
        resetFlow()
        this.host = host
        pending = Pending.Register(email.trim(), username.trim().removePrefix("@"), password)
        state = state.copy(email = email.trim(), needsCaptcha = true)
    }

    fun provideCaptcha(token: String) {
        val action = pending ?: return
        if (token.isBlank()) {
            state = state.copy(error = "Проверка безопасности не пройдена", busy = false)
            return
        }
        state = state.copy(busy = true, error = null, needsCaptcha = false)
        viewModelScope.launch {
            val result = when (action) {
                is Pending.Login -> auth.login(session, host, action.email, action.password, token)
                is Pending.Register -> auth.register(
                    session, host, action.email, action.username, action.password, token,
                )
            }
            apply(result, action is Pending.Register)
        }
    }

    fun cancelCaptcha() { state = state.copy(needsCaptcha = false) }

    fun verifyEmail(code: String) {
        if (state.busy) return
        state = state.copy(busy = true, error = null)
        viewModelScope.launch { apply(auth.verifyEmail(session, host, code), true) }
    }

    fun submitEmailCode(code: String) {
        if (state.busy) return
        state = state.copy(busy = true, error = null)
        viewModelScope.launch { apply(auth.submitEmailCode(session, host, code), false) }
    }

    fun submit2fa(code: String) {
        if (state.busy) return
        state = state.copy(busy = true, error = null)
        viewModelScope.launch { apply(auth.submit2fa(session, host, code), false) }
    }

    fun resendVerification() {
        val email = state.email
        if (email.isBlank()) return
        viewModelScope.launch { state = state.copy(error = auth.resendVerification(host, email)) }
    }

    fun resetFlow() {
        session = AuthSession()
        pending = null
        state = NativeAuthState()
    }

    fun backFromTwoFactor() { state = state.copy(step = AuthStep.EMAIL_CODE, error = null) }

    private fun apply(result: AuthStepResult, isRegistration: Boolean) {
        state = when (result) {
            AuthStepResult.Completed -> state.copy(busy = false, error = null, step = AuthStep.DONE)
            AuthStepResult.Need2fa -> state.copy(busy = false, error = null, step = AuthStep.TWO_FA)
            AuthStepResult.NeedEmailCode -> state.copy(
                busy = false, error = null, step = AuthStep.EMAIL_CODE,
                email = session.email.ifBlank { state.email },
                resendAvailable = isRegistration,
            )
            is AuthStepResult.Error -> state.copy(busy = false, error = result.message)
        }
        if (result !is AuthStepResult.Error) pending = null
    }

    private sealed interface Pending {
        data class Login(val email: String, val password: String) : Pending
        data class Register(val email: String, val username: String, val password: String) : Pending
    }

    companion object {
        fun factory(auth: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(NativeAuthViewModel::class.java)) {
                        return NativeAuthViewModel(auth) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
    }
}
