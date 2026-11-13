package ru.hyper.messenger.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.hyper.messenger.R
import ru.hyper.messenger.data.DemoData
import ru.hyper.messenger.data.LocalChatStore
import ru.hyper.messenger.ui.LocalAppContainer
import ru.hyper.messenger.ui.components.SystemBarsForTheme
import ru.hyper.messenger.ui.screens.chats.ChatsScreen
import ru.hyper.messenger.ui.screens.profile.ProfileScreen
import ru.hyper.messenger.ui.screens.settings.SettingsScreen

private enum class MainTab {
    CHATS,
    SETTINGS,
    PROFILE,
}

@Composable
fun MainScreen(
    onOpenChat: (String) -> Unit,
    onOpenThemes: () -> Unit,
    onOpenQr: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenWebSettings: () -> Unit,
    onOpenEditProfile: () -> Unit,
) {
    SystemBarsForTheme()

    val container = LocalAppContainer.current
    val currentUser by container.auth.user.collectAsStateWithLifecycle()
    val userName = currentUser?.displayName ?: DemoData.demoUser.displayName
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val saveableStateHolder = rememberSaveableStateHolder()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            HyperBottomBar(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (
                        fadeIn(tween(200)) +
                            slideInHorizontally(tween(240)) { it / 12 * direction }
                        ) togetherWith (
                        fadeOut(tween(140)) +
                            slideOutHorizontally(tween(200)) { -it / 12 * direction }
                        )
                },
                label = "main-tabs",
            ) { tab ->
                saveableStateHolder.SaveableStateProvider(tab) {
                    when (tab) {
                        0 -> ChatsScreen(
                            userName = userName,
                            onOpenChat = onOpenChat,
                            onOpenProfile = { selectedTab = 2 },
                        )

                        1 -> SettingsScreen(
                            onOpenProfile = { selectedTab = 2 },
                            onOpenThemes = onOpenThemes,
                            onOpenNotifications = onOpenNotifications,
                            onOpenPrivacy = onOpenPrivacy,
                            onOpenQr = onOpenQr,
                            onOpenWebSettings = onOpenWebSettings,
                        )

                        else -> ProfileScreen(
                            onEditProfile = onOpenEditProfile,
                            onOpenThemes = onOpenThemes,
                            onOpenQr = onOpenQr,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HyperBottomBar(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 8.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 3.dp,
            shadowElevation = 12.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(6.dp)
                    .selectableGroup(),
            ) {
                BottomBarItem(
                    selected = selectedTab == 0,
                    onClick = { onSelectTab(0) },
                    icon = Icons.Rounded.ChatBubble,
                    label = stringResource(R.string.tab_chats),
                    badge = LocalChatStore.unreadTotal().takeIf { it > 0 },
                    modifier = Modifier.weight(1f),
                )
                BottomBarItem(
                    selected = selectedTab == 1,
                    onClick = { onSelectTab(1) },
                    icon = Icons.Rounded.Settings,
                    label = stringResource(R.string.tab_settings),
                    modifier = Modifier.weight(1f),
                )
                BottomBarItem(
                    selected = selectedTab == 2,
                    onClick = { onSelectTab(2) },
                    icon = Icons.Rounded.Person,
                    label = stringResource(R.string.tab_profile),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    badge: Int? = null,
) {
    val background by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        animationSpec = tween(220),
        label = "tab-background",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(220),
        label = "tab-content",
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp),
                )
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 11.dp, y = (-6).dp)
                            .size(17.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (badge > 99) "99+" else badge.toString(),
                            color = MaterialTheme.colorScheme.onError,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
