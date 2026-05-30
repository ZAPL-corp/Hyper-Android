package ru.hyper.messenger.api

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    const val BASE_URL = "https://hyper-messenger.ru/"
    private var cookieStore: HashMap<String, List<Cookie>> = HashMap()

    fun create(context: Context): ApiService {
        val prefs = context.getSharedPreferences("hyper_session", Context.MODE_PRIVATE)

        val cookieJar = object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = cookies
                // Persist auth cookie
                val authCookie = cookies.find { it.name == "auth_user" }
                if (authCookie != null) {
                    prefs.edit().putString("auth_cookie", "${authCookie.name}=${authCookie.value}").apply()
                }
                val regCookie = cookies.find { it.name == "reg_email" }
                if (regCookie != null) {
                    prefs.edit().putString("reg_cookie", "${regCookie.name}=${regCookie.value}").apply()
                }
            }
            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val savedCookies = mutableListOf<Cookie>()
                // Load auth cookie
                val authStr = prefs.getString("auth_cookie", null)
                if (authStr != null) {
                    val (name, value) = authStr.split("=", limit = 2)
                    savedCookies.add(Cookie.Builder().name(name).value(value).domain(url.host).path("/").build())
                }
                val regStr = prefs.getString("reg_cookie", null)
                if (regStr != null && regStr.contains("=")) {
                    val idx = regStr.indexOf('=')
                    val name = regStr.substring(0, idx)
                    val value = regStr.substring(idx + 1)
                    savedCookies.add(Cookie.Builder().name(name).value(value).domain(url.host).path("/").build())
                }
                return savedCookies
            }
        }

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
