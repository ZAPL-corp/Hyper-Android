package ru.hyper.messenger.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.hyper.messenger.model.AuthResult
import ru.hyper.messenger.model.HyperUser

/**
 * Local authentication used by the interface concept.
 *
 * - The demo account signs in instantly and has no 2FA.
 * - Any other valid login demonstrates the 2FA step with the universal demo code 123456.
 * - A real network implementation should delegate to the HYPER API behind this class.
 */
class AuthRepository(
    private val preferences: PreferencesRepository,
) {
    private val _user = MutableStateFlow(preferences.readSession())
    val user: StateFlow<HyperUser?> = _user.asStateFlow()

    suspend fun login(login: String, password: String): AuthResult {
        delay(850)
        val account = login.trim()
        val isDemoAccount = account.equals(DemoData.DEMO_EMAIL, ignoreCase = true) ||
            account.equals("test", ignoreCase = true)

        if (isDemoAccount) {
            return if (password == DemoData.DEMO_PASSWORD) {
                signIn(DemoData.demoUser)
                AuthResult.Success
            } else {
                AuthResult.WrongDemoPassword
            }
        }

        if (account.length < 3) return AuthResult.InvalidLogin

        // No backend yet: other accounts show the second factor step for demonstration.
        return AuthResult.RequiresTwoFactor(account)
    }

    suspend fun verifyTwoFactor(login: String, code: String): AuthResult {
        delay(750)
        if (code != DemoData.DEMO_2FA_CODE) return AuthResult.InvalidCode

        signIn(DemoData.sessionUserFor(login))
        return AuthResult.Success
    }

    fun updateProfile(name: String, status: String) {
        val updated = preferences.updateSessionProfile(name.trim(), status.trim()) ?: return
        _user.value = updated
    }

    fun logout() {
        preferences.clearSession()
        _user.value = null
    }

    private fun signIn(user: HyperUser) {
        preferences.saveSession(user)
        _user.value = user
    }
}
