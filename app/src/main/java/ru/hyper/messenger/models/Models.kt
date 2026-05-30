package ru.hyper.messenger.models

import com.google.gson.annotations.SerializedName

data class User(
    val username: String = "",
    val email: String = "",
    val role: String = "user",
    val avatarColor: String = "#00C8D4",
    val customAvatar: String = "",
    val twoFactorEnabled: Boolean = false,
    val isPremium: Boolean = false,
    val customStatusText: String = "",
    val friendsList: List<String> = emptyList()
)

data class Friend(
    val username: String = "",
    val email: String = "",
    val avatarColor: String = "#00C8D4",
    val customAvatar: String = ""
)

data class Message(
    val id: Int = 0,
    val sender: String = "",
    val receiver: String = "",
    val message: String = "",
    val timestamp: String = ""
)

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val success: Boolean, val require2fa: Boolean = false, val email: String = "", val message: String = "")
data class Login2faRequest(val email: String, val token: String)
data class RegisterRequest(val username: String, val email: String, val password: String)
data class RegisterResponse(val success: Boolean, val message: String = "")
data class VerifyRequest(val code: String)
data class VerifyResponse(val success: Boolean, val message: String = "")
data class ApiResponse(val success: Boolean, val message: String = "", val error: String = "")

data class FriendsResponse(
    val friends: List<Friend> = emptyList(),
    val incoming: List<Friend> = emptyList(),
    val outgoing: List<Friend> = emptyList()
)

data class FriendRequestBody(val targetUsername: String)
data class ChatPreview(val username: String, val avatarColor: String, val customAvatar: String, val lastMessage: String = "", val time: String = "")
data class HistoryResponse(val messages: List<Message> = emptyList())
data class GroupsResponse(val groups: List<Group> = emptyList())

data class Group(
    val id: Int = 0,
    val name: String = "",
    val creator: String = "",
    val member_count: Int = 0
)
