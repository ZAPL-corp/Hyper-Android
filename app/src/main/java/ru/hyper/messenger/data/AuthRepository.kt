package ru.hyper.messenger.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import ru.hyper.messenger.core.ServerHost
import ru.hyper.messenger.data.remote.HyperApi
import ru.hyper.messenger.data.remote.TwoFactorSetup
import ru.hyper.messenger.model.Account
import ru.hyper.messenger.model.HyperUser

class AuthRepository(
    private val preferences: PreferencesRepository,
    private val api: HyperApi,
) {
    private val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var accounts: MutableList<Account> = preferences.readAccounts().toMutableList()
    private val storedActiveId = preferences.readActiveAccountId()
    private val initial = accounts.firstOrNull { it.user.id == storedActiveId }

    private val _user = MutableStateFlow(initial?.user)
    val user: StateFlow<HyperUser?> = _user.asStateFlow()
    private val _accountList = MutableStateFlow(accounts.map { it.user })
    val accountList: StateFlow<List<HyperUser>> = _accountList.asStateFlow()
    private val _activeAccount = MutableStateFlow(initial)
    val activeAccount: StateFlow<Account?> = _activeAccount.asStateFlow()

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)
    private var addAccountMode = false
    val isAddingAccount: Boolean get() = addAccountMode

    fun beginAddAccount() { addAccountMode = true; errorMessage.value = null }
    fun cancelAddAccount() { addAccountMode = false }
    fun clearError() { errorMessage.value = null }

    suspend fun register(
        session: AuthSession, host: ServerHost, email: String, username: String,
        password: String, captcha: String,
    ): AuthStepResult = withContext(Dispatchers.IO) {
        session.email = email
        val body = JSONObject()
            .put("email", email).put("username", username).put("password", password)
            .put("consent", true).put("age18", true).put("terms", true)
            .put("cf-turnstile-response", captcha)
        val (code, json) = api.post(host, "/api/register", body, session.jar)
        if (code in 200..299 && json?.optBoolean("success") == true) AuthStepResult.NeedEmailCode
        else AuthStepResult.Error(message(json, code, "Не удалось зарегистрироваться"))
    }

    suspend fun login(
        session: AuthSession, host: ServerHost, email: String, password: String, captcha: String,
    ): AuthStepResult = withContext(Dispatchers.IO) {
        session.email = email
        val body = JSONObject()
            .put("email", email).put("password", password)
            .put("cf-turnstile-response", captcha)
        val (code, json) = api.post(host, "/api/login", body, session.jar)
        if (code !in 200..299 || json?.optBoolean("success") != true) {
            return@withContext AuthStepResult.Error(message(json, code, "Не удалось войти"))
        }
        if (json.optBoolean("require2fa")) return@withContext AuthStepResult.Need2fa
        if (json.optBoolean("requireEmailCode")) return@withContext AuthStepResult.NeedEmailCode
        complete(session, host, json.optString("token").takeIf { it.isNotBlank() })
    }

    suspend fun verifyEmail(session: AuthSession, host: ServerHost, code: String): AuthStepResult =
        withContext(Dispatchers.IO) {
            val (status, json) = api.post(host, "/api/verify", JSONObject().put("code", code), session.jar)
            if (status !in 200..299 || json?.optBoolean("success") != true) {
                return@withContext AuthStepResult.Error(message(json, status, "Неверный код"))
            }
            complete(session, host, json.optString("token").takeIf { it.isNotBlank() })
        }

    suspend fun submitEmailCode(session: AuthSession, host: ServerHost, code: String): AuthStepResult =
        withContext(Dispatchers.IO) {
            val body = JSONObject().put("email", session.email).put("code", code)
            val (status, json) = api.post(host, "/api/login-email-code", body, session.jar)
            if (status !in 200..299 || json?.optBoolean("success") != true) {
                return@withContext AuthStepResult.Error(message(json, status, "Неверный код из письма"))
            }
            if (json.optBoolean("require2fa")) return@withContext AuthStepResult.Need2fa
            complete(session, host, json.optString("token").takeIf { it.isNotBlank() })
        }

    suspend fun submit2fa(session: AuthSession, host: ServerHost, code: String): AuthStepResult =
        withContext(Dispatchers.IO) {
            val body = JSONObject().put("email", session.email).put("token", code).put("code", code)
            val (status, json) = api.post(host, "/api/login-2fa", body, session.jar)
            if (status !in 200..299 || json?.optBoolean("success") != true) {
                return@withContext AuthStepResult.Error(message(json, status, "Неверный код 2FA"))
            }
            complete(session, host, json.optString("token").takeIf { it.isNotBlank() })
        }

    suspend fun resendVerification(host: ServerHost, email: String): String? =
        withContext(Dispatchers.IO) {
            val (status, json) = api.post(
                host, "/api/resend-verification", JSONObject().put("email", email),
            )
            if (status in 200..299 && json?.optBoolean("success") == true) null
            else json?.optString("message").orEmpty().ifBlank { "Не удалось отправить код" }
        }

    suspend fun uploadAvatar(dataUrl: String): Boolean = withContext(Dispatchers.IO) {
        val active = _activeAccount.value ?: return@withContext false
        val url = api.uploadAvatar(active.host, active.token, dataUrl) ?: return@withContext false
        val updated = active.copy(user = active.user.copy(customAvatar = url))
        replace(updated)
        _user.value = updated.user
        _activeAccount.value = updated
        true
    }

    suspend fun refreshProfile(): HyperUser? {
        val active = _activeAccount.value ?: return null
        val fresh = withContext(Dispatchers.IO) { api.me(active.host, active.token) } ?: return null
        val updated = active.copy(user = fresh)
        replace(updated)
        _user.value = fresh
        _activeAccount.value = updated
        return fresh
    }

    suspend fun updateUsername(input: String): String? {
        val active = _activeAccount.value ?: return "Нет активного аккаунта"
        val clean = input.trim().removePrefix("@")
        if (clean.length < 2 || clean.length > 32) return "Ник от 2 до 32 символов"
        val error = withContext(Dispatchers.IO) { api.updateUsername(active.host, active.token, clean) }
        if (error == null) {
            val updated = active.copy(user = active.user.copy(displayName = clean, username = "@$clean"))
            replace(updated)
            _user.value = updated.user
            _activeAccount.value = updated
        } else {
            errorMessage.value = error
        }
        return error
    }

    suspend fun setupTwoFactor(): TwoFactorSetup? {
        val active = _activeAccount.value ?: return null
        return withContext(Dispatchers.IO) { api.twoFactorSetup(active.host, active.token) }
    }

    suspend fun setTwoFactor(code: String, enable: Boolean): String? {
        val active = _activeAccount.value ?: return "Нет активного аккаунта"
        val error = withContext(Dispatchers.IO) {
            api.setTwoFactor(active.host, active.token, code, enable)
        }
        if (error == null) refreshProfile() else errorMessage.value = error
        return error
    }

    fun switchAccount(id: String) {
        val account = accounts.firstOrNull { it.user.id == id } ?: return
        _user.value = account.user
        _activeAccount.value = account
        preferences.saveActiveAccountId(account.user.id)
        addAccountMode = false
    }

    fun logout() {
        val active = _activeAccount.value ?: return
        io.launch { api.logout(active.host, active.token) }
        accounts.removeAll { it.user.id == active.user.id }
        val next = accounts.firstOrNull()
        _user.value = next?.user
        _activeAccount.value = next
        _accountList.value = accounts.map { it.user }
        preferences.saveAccounts(accounts)
        preferences.saveActiveAccountId(next?.user?.id)
        addAccountMode = false
    }

    fun removeAccount(id: String) {
        if (_activeAccount.value?.user?.id == id) { logout(); return }
        val account = accounts.firstOrNull { it.user.id == id } ?: return
        io.launch { api.logout(account.host, account.token) }
        accounts.removeAll { it.user.id == id }
        preferences.saveAccounts(accounts)
        _accountList.value = accounts.map { it.user }
    }

    fun canAddAccount(): Boolean = accounts.size < MAX_ACCOUNTS

    private suspend fun complete(session: AuthSession, host: ServerHost, token: String?): AuthStepResult {
        val bearer = token ?: api.tokenForJar(host, session.jar)
        if (bearer == null) return AuthStepResult.Error("Сервер не выдал токен")
        val user = api.me(host, bearer) ?: return AuthStepResult.Error("Не удалось загрузить профиль")
        return if (activate(Account(user, bearer, session.jar.header(), host.storageKey))) {
            addAccountMode = false
            errorMessage.value = null
            AuthStepResult.Completed
        } else {
            AuthStepResult.Error("На устройстве уже три аккаунта")
        }
    }

    private fun message(json: JSONObject?, code: Int, fallback: String): String =
        json?.optString("message").orEmpty().ifBlank { "$fallback (код $code)" }

    private fun activate(account: Account): Boolean {
        val index = accounts.indexOfFirst { it.user.id == account.user.id }
        if (index >= 0) accounts[index] = account else {
            if (accounts.size >= MAX_ACCOUNTS) return false
            accounts.add(account)
        }
        _user.value = account.user
        _activeAccount.value = account
        preferences.saveAccounts(accounts)
        preferences.saveActiveAccountId(account.user.id)
        _accountList.value = accounts.map { it.user }
        return true
    }

    private fun replace(account: Account) {
        val index = accounts.indexOfFirst { it.user.id == account.user.id }
        if (index >= 0) accounts[index] = account else accounts.add(account)
        preferences.saveAccounts(accounts)
        preferences.saveActiveAccountId(account.user.id)
        _accountList.value = accounts.map { it.user }
    }

    companion object { const val MAX_ACCOUNTS = 3 }
}
