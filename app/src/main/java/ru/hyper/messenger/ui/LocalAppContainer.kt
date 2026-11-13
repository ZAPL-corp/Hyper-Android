package ru.hyper.messenger.ui

import androidx.compose.runtime.staticCompositionLocalOf
import ru.hyper.messenger.AppContainer

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer is not provided")
}
