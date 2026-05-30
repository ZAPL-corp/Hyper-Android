package ru.hyper.messenger.utils

import android.content.Context
import ru.hyper.messenger.models.User

object SessionManager {
    private const val PREFS = "hyper_session"
    private const val KEY_AUTH_COOKIE = "auth_cookie"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_AVATAR_COLOR = "avatar_color"
    private const val KEY_CUSTOM_AVATAR = "custom_avatar"
    private const val KEY_LOGGED_IN = "logged_in"

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_LOGGED_IN, false) &&
                prefs.getString(KEY_AUTH_COOKIE, null) != null
    }

    fun saveSession(context: Context, user: User) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            putBoolean(KEY_LOGGED_IN, true)
            putString(KEY_USERNAME, user.username)
            putString(KEY_EMAIL, user.email)
            putString(KEY_AVATAR_COLOR, user.avatarColor)
            putString(KEY_CUSTOM_AVATAR, user.customAvatar)
            apply()
        }
    }

    fun getUsername(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_USERNAME, "") ?: ""

    fun getEmail(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_EMAIL, "") ?: ""

    fun getAvatarColor(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_AVATAR_COLOR, "#00C8D4") ?: "#00C8D4"

    fun getCustomAvatar(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_CUSTOM_AVATAR, "") ?: ""

    fun getAuthCookie(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_AUTH_COOKIE, "") ?: ""

    fun clearSession(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
