package ru.hyper.messenger.core

object HyperConfig {
    const val WEB_BASE_URL = "https://web.hyper-messenger.ru"
    const val ANDROID_BASE_URL = "https://android.hyper-messenger.ru"

    const val LOGIN_PATH = "/login"
    const val REGISTER_PATH = "/register"
    const val HOME_PATH = "/home"

    val SERVER_HOSTS: List<ServerHost> = listOf(ServerHost.ANDROID, ServerHost.WEB)

    const val SUPPORT_EMAIL = "support@hyper-messenger.ru"
    const val APP_VERSION_TAG = "1.1.0 concept 5"
    const val APP_VERSION = "1.1.0"
    const val APP_CHANNEL = "concept"
    const val MIN_ANDROID = 11

    fun baseUrl(host: ServerHost): String = host.baseUrl
    fun loginUrl(host: ServerHost): String = baseUrl(host) + LOGIN_PATH
    fun registerUrl(host: ServerHost): String = baseUrl(host) + REGISTER_PATH
    fun homeUrl(host: ServerHost): String = baseUrl(host) + HOME_PATH
    fun captchaUrl(host: ServerHost): String =
        baseUrl(host) + "/api/captcha/page?cb=hyper://captcha"
}

enum class ServerHost(
    val storageKey: String,
    val title: String,
    val baseUrl: String,
) {
    ANDROID("android", "Android", HyperConfig.ANDROID_BASE_URL),
    WEB("web", "Web", HyperConfig.WEB_BASE_URL);

    val host: String get() = baseUrl.removePrefix("https://")

    companion object {
        fun fromKey(key: String?): ServerHost =
            entries.firstOrNull { it.storageKey == key } ?: ANDROID
    }
}
