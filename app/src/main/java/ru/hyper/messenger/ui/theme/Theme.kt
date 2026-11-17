package ru.hyper.messenger.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
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
    val target = remember(darkTheme, dynamicColor, accentPalette) {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            else -> colorSchemeFor(accentPalette, darkTheme)
        }
    }
    val spec = tween<Color>(durationMillis = 380, easing = FastOutSlowInEasing)
    MaterialTheme(
        colorScheme = target.copy(
            primary = themeColor(target.primary, spec),
            onPrimary = themeColor(target.onPrimary, spec),
            primaryContainer = themeColor(target.primaryContainer, spec),
            onPrimaryContainer = themeColor(target.onPrimaryContainer, spec),
            secondary = themeColor(target.secondary, spec),
            onSecondary = themeColor(target.onSecondary, spec),
            secondaryContainer = themeColor(target.secondaryContainer, spec),
            onSecondaryContainer = themeColor(target.onSecondaryContainer, spec),
            background = themeColor(target.background, spec),
            onBackground = themeColor(target.onBackground, spec),
            surface = themeColor(target.surface, spec),
            onSurface = themeColor(target.onSurface, spec),
            surfaceVariant = themeColor(target.surfaceVariant, spec),
            onSurfaceVariant = themeColor(target.onSurfaceVariant, spec),
            surfaceContainerLowest = themeColor(target.surfaceContainerLowest, spec),
            surfaceContainerLow = themeColor(target.surfaceContainerLow, spec),
            surfaceContainer = themeColor(target.surfaceContainer, spec),
            surfaceContainerHigh = themeColor(target.surfaceContainerHigh, spec),
            surfaceContainerHighest = themeColor(target.surfaceContainerHighest, spec),
            outline = themeColor(target.outline, spec),
            outlineVariant = themeColor(target.outlineVariant, spec),
            error = themeColor(target.error, spec),
            errorContainer = themeColor(target.errorContainer, spec),
            onErrorContainer = themeColor(target.onErrorContainer, spec),
        ),
        typography = HyperTypography,
        shapes = HyperShapes,
        content = content,
    )
}

@Composable
private fun themeColor(color: Color, spec: AnimationSpec<Color>): Color =
    animateColorAsState(color, spec, label = "theme").value
