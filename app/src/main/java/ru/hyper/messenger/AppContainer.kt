package ru.hyper.messenger

import android.app.Application
import android.content.Context
import ru.hyper.messenger.data.AuthRepository
import ru.hyper.messenger.data.ChatRepository
import ru.hyper.messenger.data.PreferencesRepository
import ru.hyper.messenger.data.remote.HyperApi

class AppContainer(context: Context) {
    val preferences = PreferencesRepository(context)
    val api = HyperApi()
    val auth = AuthRepository(preferences, api)
    val chats = ChatRepository(preferences, api)
}

class HyperApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
