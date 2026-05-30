package ru.hyper.messenger.api

import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URI

object SocketManager {
    private var socket: Socket? = null
    var onMessage: ((String, String, String, String) -> Unit)? = null  // from, message, avatarColor, customAvatar
    var onTyping: ((String) -> Unit)? = null
    var onGroupMessage: ((Int, String, String) -> Unit)? = null  // groupId, from, message
    var onSecurityAlert: ((String) -> Unit)? = null
    var onGlobalNotification: ((String, String, String) -> Unit)? = null // from, message, chatUrl

    fun connect(username: String, sessionCookie: String) {
        try {
            val opts = IO.Options.builder()
                .setExtraHeaders(mapOf("Cookie" to listOf(sessionCookie)))
                .build()
            socket = IO.socket(URI.create("https://hyper-messenger.ru"), opts)
            socket?.connect()
            socket?.on(Socket.EVENT_CONNECT) {
                socket?.emit("identify", username)
            }
            socket?.on("msg_receive") { args ->
                val data = args[0] as? JSONObject ?: return@on
                val from = data.optString("from")
                val message = data.optString("message")
                val color = data.optString("avatarColor")
                val avatar = data.optString("customAvatar")
                onMessage?.invoke(from, message, color, avatar)
            }
            socket?.on("user_typing") { args ->
                val data = args[0] as? JSONObject ?: return@on
                onTyping?.invoke(data.optString("username"))
            }
            socket?.on("group_msg_receive") { args ->
                val data = args[0] as? JSONObject ?: return@on
                val groupId = data.optInt("groupId")
                val from = data.optString("from")
                val message = data.optString("message")
                onGroupMessage?.invoke(groupId, from, message)
            }
            socket?.on("security_alert") { args ->
                val data = args[0] as? JSONObject ?: return@on
                onSecurityAlert?.invoke(data.optString("message"))
            }
            socket?.on("global_notification") { args ->
                val data = args[0] as? JSONObject ?: return@on
                onGlobalNotification?.invoke(
                    data.optString("from"),
                    data.optString("message"),
                    data.optString("chatUrl")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendPrivateMessage(to: String, message: String, avatarColor: String, customAvatar: String) {
        val data = JSONObject().apply {
            put("to", to)
            put("message", message)
            put("senderAvatarColor", avatarColor)
            put("senderCustomAvatar", customAvatar)
        }
        socket?.emit("private_message", data)
    }

    fun sendGroupMessage(groupId: Int, message: String) {
        val data = JSONObject().apply {
            put("groupId", groupId)
            put("message", message)
        }
        socket?.emit("group_message", data)
    }

    fun emitTyping(roomId: String, isGroup: Boolean = false) {
        val data = JSONObject().apply {
            put("roomId", roomId)
            put("isGroup", isGroup)
        }
        socket?.emit("typing", data)
    }

    fun getChatHistory(withUser: String, callback: (List<JSONObject>) -> Unit) {
        socket?.once("chat_history_response") { args ->
            val data = args[0] as? JSONObject ?: return@once
            val history = data.optJSONArray("history") ?: return@once
            val list = (0 until history.length()).map { history.getJSONObject(it) }
            callback(list)
        }
        socket?.emit("get_chat_history", withUser)
    }

    fun getGroupHistory(groupId: Int, callback: (List<JSONObject>) -> Unit) {
        socket?.once("group_history_response") { args ->
            val data = args[0] as? JSONObject ?: return@once
            val history = data.optJSONArray("history") ?: return@once
            val list = (0 until history.length()).map { history.getJSONObject(it) }
            callback(list)
        }
        val req = JSONObject().apply { put("groupId", groupId) }
        socket?.emit("get_group_history", req)
    }

    fun disconnect() { socket?.disconnect(); socket = null }
    fun isConnected() = socket?.connected() == true
}
