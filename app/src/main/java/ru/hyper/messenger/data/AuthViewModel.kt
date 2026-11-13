package ru.hyper.messenger.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.hyper.messenger.model.AuthResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AuthError {
    EMPTY_LOGIN,
    EMPTY_PASSWORD,
    INVALID_LOGIN,
    WRONG_DEMO_PASSWORD,
    EMPTY_CODE,
    INVALID_CODE,
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: AuthError? = null,
    val twoFactorLogin: String? = null,
)

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    var uiState by mutableStateOf(AuthUiState())
        private set

    fun login(login: String, password: String) {
        if (uiState.isLoading) return

        val normalizedLogin = login.trim()
        when {
            normalizedLogin.isBlank() -> {
                uiState = uiState.copy(error = AuthError.EMPTY_LOGIN)
                return
            }

            password.isBlank() -> {
                uiState = uiState.copy(error = AuthError.EMPTY_PASSWORD)
                return
            }
        }

        uiState = AuthUiState(isLoading = true)
        viewModelScope.launch {
            uiState = when (val result = authRepository.login(normalizedLogin, password)) {
                AuthResult.Success -> AuthUiState()
                is AuthResult.RequiresTwoFactor -> AuthUiState(twoFactorLogin = result.login)
                AuthResult.WrongDemoPassword -> AuthUiState(error = AuthError.WRONG_DEMO_PASSWORD)
                AuthResult.InvalidLogin -> AuthUiState(error = AuthError.INVALID_LOGIN)
                AuthResult.InvalidCode -> AuthUiState(error = AuthError.INVALID_CODE)
            }
        }
    }

    fun verifyTwoFactor(login: String, code: String) {
        if (uiState.isLoading) return

        if (code.length != 6) {
            uiState = uiState.copy(error = AuthError.EMPTY_CODE)
            return
        }

        uiState = AuthUiState(isLoading = true)
        viewModelScope.launch {
            uiState = when (authRepository.verifyTwoFactor(login, code)) {
                AuthResult.Success -> AuthUiState()
                AuthResult.InvalidCode -> AuthUiState(error = AuthError.INVALID_CODE)
                else -> AuthUiState(error = AuthError.INVALID_CODE)
            }
        }
    }

    fun consumeTwoFactorLogin() {
        uiState = uiState.copy(twoFactorLogin = null)
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                        return AuthViewModel(authRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
    }
}
