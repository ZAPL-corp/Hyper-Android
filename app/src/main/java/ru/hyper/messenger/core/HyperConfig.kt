package ru.hyper.messenger.core

/**
 * All web endpoints live here so they can be switched in one place when the backend is ready.
 *
 * The original brief mentions both web.hyper-messenger.ru and web.hyper-messer.ru for registration.
 * The consistent HYPER domain is used here; change [REGISTER_URL] if the other host is intended.
 */
object HyperConfig {
    const val WEB_BASE_URL = "https://web.hyper-messenger.ru"
    const val REGISTER_URL = "$WEB_BASE_URL/register"
    const val SETTINGS_URL = "$WEB_BASE_URL/settings"
    const val LOGIN_URL = "$WEB_BASE_URL/login"

    const val SUPPORT_EMAIL = "support@hyper-messenger.ru"
    const val APP_VERSION = "1.0.0"
    const val APP_CHANNEL = "concept"
}
