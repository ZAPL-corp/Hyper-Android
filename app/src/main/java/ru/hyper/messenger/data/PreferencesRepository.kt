package ru.hyper.messenger.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import ru.hyper.messenger.core.ServerHost
import ru.hyper.messenger.model.Account
import ru.hyper.messenger.model.AccentPalette
import ru.hyper.messenger.model.HyperUser
import ru.hyper.messenger.model.ThemeMode

class PreferencesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("hyper_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        ThemeMode.entries.firstOrNull { it.name == prefs.getString("theme_mode", null) } ?: ThemeMode.SYSTEM,
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _dynamicColor = MutableStateFlow(prefs.getBoolean("dynamic_color", true))
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    private val _accentPalette = MutableStateFlow(
        AccentPalette.entries.firstOrNull { it.name == prefs.getString("accent_palette", null) } ?: AccentPalette.HYPER,
    )
    val accentPalette: StateFlow<AccentPalette> = _accentPalette.asStateFlow()

    private val _serverHost = MutableStateFlow(ServerHost.fromKey(prefs.getString("server_host", null)))
    val serverHost: StateFlow<ServerHost> = _serverHost.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _soundsEnabled = MutableStateFlow(prefs.getBoolean("sounds_enabled", true))
    val soundsEnabled: StateFlow<Boolean> = _soundsEnabled.asStateFlow()

    private val _messagePreviewEnabled = MutableStateFlow(prefs.getBoolean("message_preview_enabled", true))
    val messagePreviewEnabled: StateFlow<Boolean> = _messagePreviewEnabled.asStateFlow()

    private val _readReceiptsEnabled = MutableStateFlow(prefs.getBoolean("read_receipts_enabled", true))
    val readReceiptsEnabled: StateFlow<Boolean> = _readReceiptsEnabled.asStateFlow()

    private val _lastSeenEnabled = MutableStateFlow(prefs.getBoolean("last_seen_enabled", true))
    val lastSeenEnabled: StateFlow<Boolean> = _lastSeenEnabled.asStateFlow()

    var notificationPromptShown: Boolean
        get() = prefs.getBoolean("notification_prompt_shown", false)
        set(value) { prefs.edit().putBoolean("notification_prompt_shown", value).apply() }

    fun setThemeMode(mode: ThemeMode) { prefs.edit().putString("theme_mode", mode.name).apply(); _themeMode.value = mode }
    fun setDynamicColor(enabled: Boolean) { prefs.edit().putBoolean("dynamic_color", enabled).apply(); _dynamicColor.value = enabled }
    fun setAccentPalette(palette: AccentPalette) { prefs.edit().putString("accent_palette", palette.name).apply(); _accentPalette.value = palette }
    fun setServerHost(host: ServerHost) { prefs.edit().putString("server_host", host.storageKey).apply(); _serverHost.value = host }
    fun setNotificationsEnabled(enabled: Boolean) { prefs.edit().putBoolean("notifications_enabled", enabled).apply(); _notificationsEnabled.value = enabled }
    fun setSoundsEnabled(enabled: Boolean) { prefs.edit().putBoolean("sounds_enabled", enabled).apply(); _soundsEnabled.value = enabled }
    fun setMessagePreviewEnabled(enabled: Boolean) { prefs.edit().putBoolean("message_preview_enabled", enabled).apply(); _messagePreviewEnabled.value = enabled }
    fun setReadReceiptsEnabled(enabled: Boolean) { prefs.edit().putBoolean("read_receipts_enabled", enabled).apply(); _readReceiptsEnabled.value = enabled }
    fun setLastSeenEnabled(enabled: Boolean) { prefs.edit().putBoolean("last_seen_enabled", enabled).apply(); _lastSeenEnabled.value = enabled }

    fun readAccounts(): List<Account> {
        val raw = prefs.getString("accounts", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val json = array.optJSONObject(index) ?: continue
                    val token = json.optString("token")
                    val email = json.optString("email")
                    if (token.isBlank() || email.isBlank()) continue
                    add(
                        Account(
                            user = HyperUser(
                                id = email,
                                displayName = json.optString("name"),
                                username = json.optString("username"),
                                email = email,
                                status = json.optString("status"),
                                avatarColor = json.optString("avatarColor"),
                                customAvatar = json.optString("customAvatar"),
                                twoFactorEnabled = json.optBoolean("twofa", false),
                            ),
                            token = token,
                            cookie = json.optString("cookie"),
                            hostKey = json.optString("host", ServerHost.ANDROID.storageKey),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveAccounts(accounts: List<Account>) {
        val array = JSONArray()
        accounts.forEach { account ->
            array.put(
                JSONObject()
                    .put("email", account.user.email)
                    .put("name", account.user.displayName)
                    .put("username", account.user.username)
                    .put("status", account.user.status)
                    .put("avatarColor", account.user.avatarColor)
                    .put("customAvatar", account.user.customAvatar)
                    .put("twofa", account.user.twoFactorEnabled)
                    .put("token", account.token)
                    .put("cookie", account.cookie)
                    .put("host", account.hostKey),
            )
        }
        prefs.edit().putString("accounts", array.toString()).apply()
    }

    fun readActiveAccountId(): String? = prefs.getString("active_account_id", null)

    fun saveActiveAccountId(id: String?) {
        val editor = prefs.edit()
        if (id == null) editor.remove("active_account_id") else editor.putString("active_account_id", id)
        editor.apply()
    }

    fun readCache(key: String): String? = prefs.getString(key, null)
    fun writeCache(key: String, value: String) { prefs.edit().putString(key, value).apply() }
}
