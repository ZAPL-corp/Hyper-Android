package ru.hyper.messenger.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.hyper.messenger.model.AccentPalette
import ru.hyper.messenger.model.HyperUser
import ru.hyper.messenger.model.ThemeMode

/**
 * Lightweight local preferences for the concept. Auth tokens are intentionally not stored yet;
 * when the API is connected this can be replaced by DataStore + encrypted storage.
 */
class PreferencesRepository(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        ThemeMode.entries.firstOrNull { it.name == prefs.getString(KEY_THEME_MODE, null) }
            ?: ThemeMode.SYSTEM,
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _dynamicColor = MutableStateFlow(prefs.getBoolean(KEY_DYNAMIC_COLOR, true))
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    private val _accentPalette = MutableStateFlow(
        AccentPalette.entries.firstOrNull { it.name == prefs.getString(KEY_ACCENT, null) }
            ?: AccentPalette.HYPER,
    )
    val accentPalette: StateFlow<AccentPalette> = _accentPalette.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATIONS, true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _soundsEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUNDS, true))
    val soundsEnabled: StateFlow<Boolean> = _soundsEnabled.asStateFlow()

    private val _messagePreviewEnabled = MutableStateFlow(prefs.getBoolean(KEY_PREVIEW, true))
    val messagePreviewEnabled: StateFlow<Boolean> = _messagePreviewEnabled.asStateFlow()

    private val _readReceiptsEnabled = MutableStateFlow(prefs.getBoolean(KEY_READ_RECEIPTS, true))
    val readReceiptsEnabled: StateFlow<Boolean> = _readReceiptsEnabled.asStateFlow()

    private val _lastSeenEnabled = MutableStateFlow(prefs.getBoolean(KEY_LAST_SEEN, true))
    val lastSeenEnabled: StateFlow<Boolean> = _lastSeenEnabled.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _dynamicColor.value = enabled
    }

    fun setAccentPalette(palette: AccentPalette) {
        prefs.edit().putString(KEY_ACCENT, palette.name).apply()
        _accentPalette.value = palette
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
        _notificationsEnabled.value = enabled
    }

    fun setSoundsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUNDS, enabled).apply()
        _soundsEnabled.value = enabled
    }

    fun setMessagePreviewEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PREVIEW, enabled).apply()
        _messagePreviewEnabled.value = enabled
    }

    fun setReadReceiptsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_READ_RECEIPTS, enabled).apply()
        _readReceiptsEnabled.value = enabled
    }

    fun setLastSeenEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LAST_SEEN, enabled).apply()
        _lastSeenEnabled.value = enabled
    }

    fun readSession(): HyperUser? {
        val email = prefs.getString(KEY_SESSION_EMAIL, null) ?: return null
        return HyperUser(
            id = prefs.getString(KEY_SESSION_ID, null) ?: "local-user",
            displayName = prefs.getString(KEY_SESSION_NAME, null) ?: "HYPER User",
            username = prefs.getString(KEY_SESSION_USERNAME, null) ?: "@hyper_user",
            email = email,
            status = prefs.getString(KEY_SESSION_STATUS, null) ?: "На связи в HYPER",
            isDemo = prefs.getBoolean(KEY_SESSION_DEMO, false),
        )
    }

    fun saveSession(user: HyperUser) {
        prefs.edit()
            .putString(KEY_SESSION_ID, user.id)
            .putString(KEY_SESSION_NAME, user.displayName)
            .putString(KEY_SESSION_USERNAME, user.username)
            .putString(KEY_SESSION_EMAIL, user.email)
            .putString(KEY_SESSION_STATUS, user.status)
            .putBoolean(KEY_SESSION_DEMO, user.isDemo)
            .apply()
    }

    fun updateSessionProfile(name: String, status: String): HyperUser? {
        val current = readSession() ?: return null
        val updated = current.copy(displayName = name, status = status)
        saveSession(updated)
        return updated
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_SESSION_ID)
            .remove(KEY_SESSION_NAME)
            .remove(KEY_SESSION_USERNAME)
            .remove(KEY_SESSION_EMAIL)
            .remove(KEY_SESSION_STATUS)
            .remove(KEY_SESSION_DEMO)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "hyper_prefs"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_DYNAMIC_COLOR = "dynamic_color"
        const val KEY_ACCENT = "accent_palette"
        const val KEY_NOTIFICATIONS = "notifications_enabled"
        const val KEY_SOUNDS = "sounds_enabled"
        const val KEY_PREVIEW = "message_preview_enabled"
        const val KEY_READ_RECEIPTS = "read_receipts_enabled"
        const val KEY_LAST_SEEN = "last_seen_enabled"
        const val KEY_SESSION_ID = "session_id"
        const val KEY_SESSION_NAME = "session_name"
        const val KEY_SESSION_USERNAME = "session_username"
        const val KEY_SESSION_EMAIL = "session_email"
        const val KEY_SESSION_STATUS = "session_status"
        const val KEY_SESSION_DEMO = "session_demo"
    }
}
