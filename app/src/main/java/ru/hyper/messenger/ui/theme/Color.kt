package ru.hyper.messenger.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import ru.hyper.messenger.model.AccentPalette

val HyperNavy = Color(0xFF061427)
val HyperBlue = Color(0xFF0A84FF)
val HyperCyan = Color(0xFF19C3FF)
val HyperIce = Color(0xFFE8F4FF)
val HyperGreen = Color(0xFF34C759)
val HyperRed = Color(0xFFE5484D)

private data class BrandPalette(
    val primary: Long,
    val onPrimary: Long,
    val primaryContainer: Long,
    val onPrimaryContainer: Long,
    val secondary: Long,
    val tertiary: Long,
    val surface: Long,
    val onSurface: Long,
    val surfaceVariant: Long,
    val onSurfaceVariant: Long,
    val outline: Long,
)

private data class PaletteSet(
    val light: BrandPalette,
    val dark: BrandPalette,
)

private val palettes: Map<AccentPalette, PaletteSet> = mapOf(
    AccentPalette.HYPER to PaletteSet(
        light = BrandPalette(
            primary = 0xFF0061A8,
            onPrimary = 0xFFFFFFFF,
            primaryContainer = 0xFFD1E4FF,
            onPrimaryContainer = 0xFF001D36,
            secondary = 0xFF00639B,
            tertiary = 0xFF006874,
            surface = 0xFFF7FAFD,
            onSurface = 0xFF171C22,
            surfaceVariant = 0xFFE0E8F1,
            onSurfaceVariant = 0xFF414A52,
            outline = 0xFF717A83,
        ),
        dark = BrandPalette(
            primary = 0xFF9FCBFF,
            onPrimary = 0xFF003258,
            primaryContainer = 0xFF00497D,
            onPrimaryContainer = 0xFFD1E4FF,
            secondary = 0xFF8FCDFF,
            tertiary = 0xFF4FD8EB,
            surface = 0xFF0E1419,
            onSurface = 0xFFDEE3E9,
            surfaceVariant = 0xFF3F4850,
            onSurfaceVariant = 0xFFBFC8D1,
            outline = 0xFF89929B,
        ),
    ),
    AccentPalette.VIOLET to PaletteSet(
        light = BrandPalette(
            primary = 0xFF6750A4,
            onPrimary = 0xFFFFFFFF,
            primaryContainer = 0xFFEADDFF,
            onPrimaryContainer = 0xFF21005D,
            secondary = 0xFF625B71,
            tertiary = 0xFF7D5260,
            surface = 0xFFFBF8FF,
            onSurface = 0xFF1C1B1F,
            surfaceVariant = 0xFFE7E0EC,
            onSurfaceVariant = 0xFF49454F,
            outline = 0xFF79747E,
        ),
        dark = BrandPalette(
            primary = 0xFFD0BCFF,
            onPrimary = 0xFF381E72,
            primaryContainer = 0xFF4F378B,
            onPrimaryContainer = 0xFFEADDFF,
            secondary = 0xFFCCC2DC,
            tertiary = 0xFFEFB8C8,
            surface = 0xFF141218,
            onSurface = 0xFFE6E1E5,
            surfaceVariant = 0xFF49454F,
            onSurfaceVariant = 0xFFCAC4CF,
            outline = 0xFF938F99,
        ),
    ),
    AccentPalette.EMERALD to PaletteSet(
        light = BrandPalette(
            primary = 0xFF006B57,
            onPrimary = 0xFFFFFFFF,
            primaryContainer = 0xFF8CF8D4,
            onPrimaryContainer = 0xFF002117,
            secondary = 0xFF4A635C,
            tertiary = 0xFF3D6373,
            surface = 0xFFF5FBF7,
            onSurface = 0xFF171D1B,
            surfaceVariant = 0xFFDBE5DE,
            onSurfaceVariant = 0xFF404944,
            outline = 0xFF707974,
        ),
        dark = BrandPalette(
            primary = 0xFF6FDBB9,
            onPrimary = 0xFF00382C,
            primaryContainer = 0xFF005141,
            onPrimaryContainer = 0xFF8CF8D4,
            secondary = 0xFFB1CCC3,
            tertiary = 0xFFA5CDDF,
            surface = 0xFF0E1512,
            onSurface = 0xFFDDE4DF,
            surfaceVariant = 0xFF3F4944,
            onSurfaceVariant = 0xFFBFC9C2,
            outline = 0xFF89938D,
        ),
    ),
    AccentPalette.SUNSET to PaletteSet(
        light = BrandPalette(
            primary = 0xFF9A4300,
            onPrimary = 0xFFFFFFFF,
            primaryContainer = 0xFFFFDBC8,
            onPrimaryContainer = 0xFF341100,
            secondary = 0xFF7C5635,
            tertiary = 0xFF6C5D2F,
            surface = 0xFFFFF8F5,
            onSurface = 0xFF211A17,
            surfaceVariant = 0xFFF1DFD5,
            onSurfaceVariant = 0xFF51443D,
            outline = 0xFF837469,
        ),
        dark = BrandPalette(
            primary = 0xFFFFB68E,
            onPrimary = 0xFF522300,
            primaryContainer = 0xFF743200,
            onPrimaryContainer = 0xFFFFDBC8,
            secondary = 0xFFE7BFA8,
            tertiary = 0xFFDBC48A,
            surface = 0xFF1A110B,
            onSurface = 0xFFF1DFD5,
            surfaceVariant = 0xFF51443D,
            onSurfaceVariant = 0xFFD5C2B7,
            outline = 0xFF9F8D82,
        ),
    ),
    AccentPalette.ROSE to PaletteSet(
        light = BrandPalette(
            primary = 0xFF984061,
            onPrimary = 0xFFFFFFFF,
            primaryContainer = 0xFFFFD9E2,
            onPrimaryContainer = 0xFF3E001D,
            secondary = 0xFF75565E,
            tertiary = 0xFF815343,
            surface = 0xFFFFF8F8,
            onSurface = 0xFF211A1C,
            surfaceVariant = 0xFFF0DEE3,
            onSurfaceVariant = 0xFF514348,
            outline = 0xFF837378,
        ),
        dark = BrandPalette(
            primary = 0xFFFFB1C8,
            onPrimary = 0xFF5E1133,
            primaryContainer = 0xFF7D294A,
            onPrimaryContainer = 0xFFFFD9E2,
            secondary = 0xFFE4BDC6,
            tertiary = 0xFFF4B9A6,
            surface = 0xFF191113,
            onSurface = 0xFFECE0E2,
            surfaceVariant = 0xFF514348,
            onSurfaceVariant = 0xFFD5C2C7,
            outline = 0xFF9E8C91,
        ),
    ),
)

fun colorSchemeFor(accentPalette: AccentPalette, darkTheme: Boolean): ColorScheme {
    val set = palettes.getValue(accentPalette)
    return if (darkTheme) set.dark.toDarkScheme() else set.light.toLightScheme()
}

fun swatchColor(accentPalette: AccentPalette): Color = when (accentPalette) {
    AccentPalette.HYPER -> Color(0xFF0A84FF)
    AccentPalette.VIOLET -> Color(0xFF8B72E8)
    AccentPalette.EMERALD -> Color(0xFF00A884)
    AccentPalette.SUNSET -> Color(0xFFFF7A45)
    AccentPalette.ROSE -> Color(0xFFE75480)
}

private fun BrandPalette.toLightScheme(): ColorScheme = lightColorScheme(
    primary = Color(primary),
    onPrimary = Color(onPrimary),
    primaryContainer = Color(primaryContainer),
    onPrimaryContainer = Color(onPrimaryContainer),
    inversePrimary = Color(primaryContainer),
    secondary = Color(secondary),
    onSecondary = Color.White,
    secondaryContainer = Color(primaryContainer),
    onSecondaryContainer = Color(onPrimaryContainer),
    tertiary = Color(tertiary),
    onTertiary = Color.White,
    tertiaryContainer = Color(primaryContainer),
    onTertiaryContainer = Color(onPrimaryContainer),
    background = Color(surface),
    onBackground = Color(onSurface),
    surface = Color(surface),
    onSurface = Color(onSurface),
    surfaceVariant = Color(surfaceVariant),
    onSurfaceVariant = Color(onSurfaceVariant),
    surfaceTint = Color(primary),
    inverseSurface = Color(onSurface),
    inverseOnSurface = Color(surface),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(outline),
    outlineVariant = Color(surfaceVariant),
    scrim = Color.Black,
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFE4E9F0),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F9FC),
    surfaceContainer = Color(0xFFF1F4F9),
    surfaceContainerHigh = Color(0xFFEBEFF5),
    surfaceContainerHighest = Color(0xFFE4E9F0),
)

private fun BrandPalette.toDarkScheme(): ColorScheme = darkColorScheme(
    primary = Color(primary),
    onPrimary = Color(onPrimary),
    primaryContainer = Color(primaryContainer),
    onPrimaryContainer = Color(onPrimaryContainer),
    inversePrimary = Color(primaryContainer),
    secondary = Color(secondary),
    onSecondary = Color(onPrimary),
    secondaryContainer = Color(primaryContainer),
    onSecondaryContainer = Color(onPrimaryContainer),
    tertiary = Color(tertiary),
    onTertiary = Color(onPrimary),
    tertiaryContainer = Color(primaryContainer),
    onTertiaryContainer = Color(onPrimaryContainer),
    background = Color(surface),
    onBackground = Color(onSurface),
    surface = Color(surface),
    onSurface = Color(onSurface),
    surfaceVariant = Color(surfaceVariant),
    onSurfaceVariant = Color(onSurfaceVariant),
    surfaceTint = Color(primary),
    inverseSurface = Color(onSurface),
    inverseOnSurface = Color(surface),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(outline),
    outlineVariant = Color(surfaceVariant),
    scrim = Color.Black,
    surfaceBright = Color(0xFF353C44),
    surfaceDim = Color(surface),
    surfaceContainerLowest = Color(0xFF080C10),
    surfaceContainerLow = Color(0xFF12181E),
    surfaceContainer = Color(0xFF171D23),
    surfaceContainerHigh = Color(0xFF212830),
    surfaceContainerHighest = Color(0xFF2B333B),
)
