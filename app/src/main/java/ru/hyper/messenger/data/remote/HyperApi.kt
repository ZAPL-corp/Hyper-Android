package ru.hyper.messenger.data.remote

import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject
import ru.hyper.messenger.core.ServerHost
import ru.hyper.messenger.model.HyperUser

data class Friend(
    val username: String,
    val email: String,
    val avatarColor: String,
    val customAvatar: String,
)

data class RemoteGroup(val id: Long, val name: String, val members: Int)
data class TwoFactorSetup(val secret: String, val otpauth: String)

class HyperApi {
    private val timeout = 20_000

    fun tokenForCookie(host: ServerHost, cookie: String): String? {
        val (code, json) = request(host, "/api/auth/token", "POST", cookie = cookie, body = JSONObject())
        if (code != 200) return null
        return json?.optString("token")?.takeIf { it.isNotBlank() }
    }

    fun tokenForJar(host: ServerHost, jar: CookieJar): String? = tokenForCookie(host, jar.header())

    fun post(host: ServerHost, path: String, body: JSONObject, jar: CookieJar? = null): Pair<Int, JSONObject?> =
        request(host, path, "POST", cookie = jar?.header(), body = body, jar = jar)

    fun me(host: ServerHost, token: String): HyperUser? {
        val (code, json) = request(host, "/api/me", "GET", token = token)
        if (code != 200 || json == null) return null
        val username = json.optString("username").ifBlank { return null }
        val email = json.optString("email")
        return HyperUser(
            id = email.ifBlank { username },
            displayName = username,
            username = "@$username",
            email = email,
            status = json.optString("customStatusText"),
            avatarColor = json.optString("avatarColor"),
            customAvatar = absolute(host, json.optString("customAvatar")),
            twoFactorEnabled = json.optBoolean("twoFactorEnabled"),
        )
    }

    fun friends(host: ServerHost, token: String): List<Friend> {
        val (code, json) = request(host, "/api/friends/list", "GET", token = token)
        if (code != 200) return emptyList()
        val array = json?.optJSONArray("friends") ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val username = item.optString("username")
                if (username.isBlank()) continue
                add(
                    Friend(
                        username = username,
                        email = item.optString("email"),
                        avatarColor = item.optString("avatarColor"),
                        customAvatar = absolute(host, item.optString("customAvatar")),
                    ),
                )
            }
        }
    }

    fun findUser(host: ServerHost, token: String, username: String): Friend? {
        val body = JSONObject().put("usernames", JSONArray().put(username))
        val (code, json) = request(host, "/api/users/batch", "POST", token = token, body = body)
        if (code != 200) return null
        val item = json?.optJSONArray("users")?.optJSONObject(0) ?: return null
        return Friend(
            username = item.optString("username"),
            email = "",
            avatarColor = item.optString("avatarColor"),
            customAvatar = absolute(host, item.optString("customAvatar")),
        )
    }

    fun groups(host: ServerHost, token: String): List<RemoteGroup> {
        val (code, json) = request(host, "/api/groups/list", "GET", token = token)
        if (code != 200) return emptyList()
        val array = json?.optJSONArray("groups") ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val id = item.optLong("id", 0L)
                if (id <= 0L) continue
                add(RemoteGroup(id, item.optString("name"), item.optInt("member_count")))
            }
        }
    }

    fun updateUsername(host: ServerHost, token: String, username: String): String? {
        val (code, json) = request(
            host, "/api/update-profile", "POST", token = token,
            body = JSONObject().put("username", username),
        )
        return if (code == 200 && json?.optBoolean("success") == true) null
        else json?.optString("message").orEmpty().ifBlank { "Не удалось сохранить ник" }
    }

    fun createGroup(host: ServerHost, token: String, name: String, members: List<String>): Long? {
        val body = JSONObject().put("name", name).put("members", JSONArray(members))
        val (code, json) = request(host, "/api/groups/create", "POST", token = token, body = body)
        if (code != 200) return null
        return json?.optLong("groupId", 0L)?.takeIf { it > 0L }
    }

    fun uploadAvatar(host: ServerHost, token: String, dataUrl: String): String? {
        val body = JSONObject().put("image", dataUrl)
        val (code, json) = request(host, "/api/avatar", "POST", token = token, body = body)
        if (code != 200) return null
        return absolute(host, json?.optString("customAvatar").orEmpty())
    }

    fun twoFactorSetup(host: ServerHost, token: String): TwoFactorSetup? {
        val (code, json) = request(host, "/api/2fa/setup", "POST", token = token, body = JSONObject())
        if (code != 200 || json == null) return null
        val secret = json.optString("secret")
        if (secret.isBlank()) return null
        return TwoFactorSetup(secret, json.optString("qrCode"))
    }

    fun setTwoFactor(host: ServerHost, token: String, otp: String, enable: Boolean): String? {
        val body = JSONObject().put("token", otp).put("enable", enable)
        val (code, json) = request(host, "/api/2fa/verify-toggle", "POST", token = token, body = body)
        return if (code == 200) null else json?.optString("message").orEmpty().ifBlank { "Ошибка 2FA" }
    }

    fun logout(host: ServerHost, token: String) {
        request(host, "/api/auth/logout", "POST", token = token, body = JSONObject())
    }

    private fun absolute(host: ServerHost, url: String): String =
        if (url.startsWith("/")) host.baseUrl + url else url

    private fun request(
        host: ServerHost,
        path: String,
        method: String,
        token: String? = null,
        cookie: String? = null,
        body: JSONObject? = null,
        jar: CookieJar? = null,
    ): Pair<Int, JSONObject?> = runCatching {
        val connection = open(host, path, method, token, cookie, body)
        val code = connection.responseCode
        jar?.store(connection)
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
        connection.disconnect()
        code to runCatching { JSONObject(text) }.getOrNull()
    }.getOrDefault(0 to null)

    private fun open(
        host: ServerHost,
        path: String,
        method: String,
        token: String?,
        cookie: String?,
        body: JSONObject?,
    ): HttpURLConnection {
        val connection = URL(host.baseUrl + path).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = timeout
        connection.readTimeout = timeout
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Accept-Encoding", "identity")
        connection.setRequestProperty("User-Agent", "HYPER-Android/1.1")
        if (token != null) connection.setRequestProperty("Authorization", "Bearer $token")
        if (cookie != null) connection.setRequestProperty("Cookie", cookie)
        if (body != null && method != "GET") {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        }
        return connection
    }
}
