package ru.hyper.messenger.api

import ru.hyper.messenger.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("api/login-2fa")
    suspend fun login2fa(@Body body: Login2faRequest): Response<LoginResponse>

    @POST("api/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("api/verify")
    suspend fun verify(@Body body: VerifyRequest): Response<VerifyResponse>

    @GET("api/me")
    suspend fun getMe(): Response<ru.hyper.messenger.models.User>

    @GET("api/friends/list")
    suspend fun getFriends(): Response<FriendsResponse>

    @POST("api/friends/request")
    suspend fun sendFriendRequest(@Body body: FriendRequestBody): Response<ApiResponse>

    @POST("api/friends/accept")
    suspend fun acceptFriend(@Body body: FriendRequestBody): Response<ApiResponse>

    @POST("api/friends/reject")
    suspend fun rejectFriend(@Body body: FriendRequestBody): Response<ApiResponse>

    @POST("api/logout")
    suspend fun logout(): Response<ApiResponse>
}
