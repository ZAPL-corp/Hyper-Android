package ru.hyper.messenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.hyper.messenger.model.ThemeMode
import ru.hyper.messenger.ui.HyperRoot
import ru.hyper.messenger.ui.theme.HyperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as HyperApplication).container

        setContent {
            val systemDark = isSystemInDarkTheme()
            val themeMode by container.preferences.themeMode.collectAsStateWithLifecycle()
            val dynamicColor by container.preferences.dynamicColor.collectAsStateWithLifecycle()
            val accentPalette by container.preferences.accentPalette.collectAsStateWithLifecycle()

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            HyperTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColor,
                accentPalette = accentPalette,
            ) {
                HyperRoot(container = container)
            }
        }
    }
}
