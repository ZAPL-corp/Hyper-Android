package ru.hyper.messenger

import android.app.Application
import android.content.Context
import ru.hyper.messenger.data.AuthRepository
import ru.hyper.messenger.data.PreferencesRepository

class AppContainer(context: Context) {
    val preferences = PreferencesRepository(context)
    val auth = AuthRepository(preferences)
}

class HyperApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
