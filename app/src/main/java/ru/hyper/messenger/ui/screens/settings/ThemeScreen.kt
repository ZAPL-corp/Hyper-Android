package ru.hyper.messenger.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.hyper.messenger.R
import ru.hyper.messenger.model.AccentPalette
import ru.hyper.messenger.model.ThemeMode
import ru.hyper.messenger.ui.LocalAppContainer
import ru.hyper.messenger.ui.components.DemoBadge
import ru.hyper.messenger.ui.components.HyperAvatar
import ru.hyper.messenger.ui.components.HyperBackTopBar
import ru.hyper.messenger.ui.components.SectionTitle
import ru.hyper.messenger.ui.components.SettingsGroup
import ru.hyper.messenger.ui.components.SettingsRow
import ru.hyper.messenger.ui.components.SystemBarsForTheme
import ru.hyper.messenger.ui.theme.swatchColor

@Composable
fun ThemeScreen(onBack: () -> Unit) {
    SystemBarsForTheme()

    val prefs = LocalAppContainer.current.preferences
    val themeMode by prefs.themeMode.collectAsStateWithLifecycle()
    val dynamicColor by prefs.dynamicColor.collectAsStateWithLifecycle()
    val accentPalette by prefs.accentPalette.collectAsStateWithLifecycle()
    val dynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val accentsEnabled = !(dynamicSupported && dynamicColor)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HyperBackTopBar(
                title = stringResource(R.string.themes_title),
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.themes_headline),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.themes_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(22.dp))

            SectionTitle(text = stringResource(R.string.theme_mode_label))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeMode.entries.forEach { mode ->
                    ThemeModeCard(
                        mode = mode,
                        selected = themeMode == mode,
                        onClick = { prefs.setThemeMode(mode) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            SettingsGroup {
                SettingsRow(
                    icon = Icons.Rounded.Palette,
                    title = stringResource(R.string.theme_dynamic),
                    subtitle = stringResource(
                        if (dynamicSupported) {
                            R.string.theme_dynamic_desc
                        } else {
                            R.string.theme_dynamic_unavailable
                        },
                    ),
                    onClick = {
                        if (dynamicSupported) prefs.setDynamicColor(!dynamicColor)
                    },
                    trailing = {
                        Switch(
                            checked = dynamicSupported && dynamicColor,
                            onCheckedChange = prefs::setDynamicColor,
                            enabled = dynamicSupported,
                        )
                    },
                )
            }

            Spacer(Modifier.height(22.dp))

            SectionTitle(text = stringResource(R.string.theme_accent))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (accentsEnabled) 1f else 0.45f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AccentPalette.entries.forEach { palette ->
                    AccentSwatch(
                        palette = palette,
                        selected = accentPalette == palette,
                        enabled = accentsEnabled,
                        onClick = { prefs.setAccentPalette(palette) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (dynamicSupported && dynamicColor) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.theme_dynamic_active_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.theme_preview),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                DemoBadge(text = stringResource(R.string.theme_preview_badge))
            }

            Spacer(Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HyperAvatar(name = "Мария Соколова", size = 38.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Мария Соколова",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = stringResource(R.string.chat_online),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Text(
                            text = stringResource(R.string.theme_preview_time),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = 5.dp,
                                bottomEnd = 18.dp,
                            ),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            Text(
                                text = stringResource(R.string.theme_preview_message),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = 18.dp,
                                bottomEnd = 5.dp,
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Отлично! ✨",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Rounded.DoneAll,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(
                                text = stringResource(R.string.chat_input_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DoneAll,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    text = stringResource(R.string.done),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ThemeModeCard(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon = when (mode) {
        ThemeMode.SYSTEM -> Icons.Rounded.SettingsBrightness
        ThemeMode.LIGHT -> Icons.Rounded.LightMode
        ThemeMode.DARK -> Icons.Rounded.DarkMode
    }
    val label = when (mode) {
        ThemeMode.SYSTEM -> R.string.theme_system
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
            )
            .border(
                width = if (selected) 1.5.dp else 0.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AccentSwatch(
    palette: AccentPalette,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when (palette) {
        AccentPalette.HYPER -> R.string.theme_accent_hyper
        AccentPalette.VIOLET -> R.string.theme_accent_violet
        AccentPalette.EMERALD -> R.string.theme_accent_emerald
        AccentPalette.SUNSET -> R.string.theme_accent_sunset
        AccentPalette.ROSE -> R.string.theme_accent_rose
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(swatchColor(palette))
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
