package ru.hyper.messenger.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ru.hyper.messenger.model.AccentPalette

@Composable
fun HyperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    accentPalette: AccentPalette = AccentPalette.HYPER,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> colorSchemeFor(accentPalette, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HyperTypography,
        shapes = HyperShapes,
        content = content,
    )
}
