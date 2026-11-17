package ru.hyper.messenger.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import ru.hyper.messenger.data.AuthRepository
import ru.hyper.messenger.model.HyperUser
import ru.hyper.messenger.ui.LocalAppContainer
import ru.hyper.messenger.ui.components.HyperAvatar
import ru.hyper.messenger.ui.components.SystemBarsForTheme
import ru.hyper.messenger.ui.screens.chats.ChatsScreen
import ru.hyper.messenger.ui.screens.profile.ProfileScreen
import ru.hyper.messenger.ui.screens.settings.SettingsScreen

@Composable
fun MainScreen(
    onOpenChat: (String) -> Unit,
    onOpenThemes: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenWebSettings: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onOpenSecurity: () -> Unit,
    onAddAccount: () -> Unit,
) {
    SystemBarsForTheme()
    val container = LocalAppContainer.current
    val currentUser by container.auth.user.collectAsStateWithLifecycle()
    val accounts by container.auth.accountList.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showAccounts by rememberSaveable { mutableStateOf(false) }
    val holder = rememberSaveableStateHolder()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            HyperBottomBar(
                selectedTab = selectedTab,
                unreadTotal = container.chats.unreadTotal(),
                onSelectTab = { selectedTab = it },
                onLongPressProfile = { showAccounts = true },
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (fadeIn(tween(200)) + slideInHorizontally(tween(240)) { it / 12 * dir }) togetherWith
                        (fadeOut(tween(140)) + slideOutHorizontally(tween(200)) { -it / 12 * dir })
                },
                label = "tabs",
            ) { tab ->
                holder.SaveableStateProvider(tab) {
                    when (tab) {
                        0 -> ChatsScreen(
                            userName = currentUser?.displayName ?: "HYPER",
                            onOpenChat = onOpenChat,
                            onOpenProfile = { selectedTab = 2 },
                        )
                        1 -> SettingsScreen(
                            onOpenProfile = { selectedTab = 2 },
                            onOpenThemes = onOpenThemes,
                            onOpenNotifications = onOpenNotifications,
                            onOpenPrivacy = onOpenPrivacy,
                            onOpenSecurity = onOpenSecurity,
                            onOpenWebSettings = onOpenWebSettings,
                        )
                        else -> ProfileScreen(
                            onEditProfile = onOpenEditProfile,
                            onOpenThemes = onOpenThemes,
                            onOpenSecurity = onOpenSecurity,
                            onOpenAccounts = { showAccounts = true },
                        )
                    }
                }
            }
        }
    }

    if (showAccounts) {
        AccountsSheet(
            accounts = accounts,
            activeId = currentUser?.id,
            canAdd = accounts.size < AuthRepository.MAX_ACCOUNTS,
            onSwitch = { container.auth.switchAccount(it); showAccounts = false },
            onAdd = { showAccounts = false; onAddAccount() },
            onRemove = { container.auth.removeAccount(it) },
            onLogout = { showAccounts = false; container.auth.logout() },
            onDismiss = { showAccounts = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun AccountsSheet(
    accounts: List<HyperUser>,
    activeId: String?,
    canAdd: Boolean,
    onSwitch: (String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 22.dp),
        ) {
            Text(stringResource(R.string.accounts_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.accounts_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))

            accounts.forEach { account ->
                val active = account.id == activeId
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .combinedClickable(onClick = { if (!active) onSwitch(account.id) }, onLongClick = null)
                        .background(if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HyperAvatar(
                        name = account.displayName,
                        size = 46.dp,
                        imageUrl = account.customAvatar.takeIf { it.isNotBlank() },
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(account.displayName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(account.username, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (active) Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    else androidx.compose.material3.TextButton(onClick = { onRemove(account.id) }) {
                        Text(stringResource(R.string.accounts_remove), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .combinedClickable(onClick = { if (canAdd) onAdd() }, onLongClick = null)
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(46.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.accounts_add), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(if (canAdd) R.string.accounts_add_hint else R.string.accounts_limit),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .combinedClickable(onClick = onLogout, onLongClick = null)
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(46.dp).clip(CircleShape).background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                }
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.settings_logout), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private var removeAccount: ((String) -> Unit)? = null

private fun containerRemove(id: String) { removeAccount?.invoke(id) }

@Composable
private fun TextButtonSmall(text: String, onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun HyperBottomBar(
    selectedTab: Int,
    unreadTotal: Int,
    onSelectTab: (Int) -> Unit,
    onLongPressProfile: () -> Unit,
) {
    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 8.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 3.dp,
            shadowElevation = 12.dp,
        ) {
            Row(Modifier.fillMaxWidth().height(68.dp).padding(6.dp).selectableGroup()) {
                BottomBarItem(selectedTab == 0, { onSelectTab(0) }, Icons.Rounded.ChatBubble, stringResource(R.string.tab_chats), Modifier.weight(1f), unreadTotal.takeIf { it > 0 })
                BottomBarItem(selectedTab == 1, { onSelectTab(1) }, Icons.Rounded.Settings, stringResource(R.string.tab_settings), Modifier.weight(1f))
                BottomBarItem(selectedTab == 2, { onSelectTab(2) }, Icons.Rounded.Person, stringResource(R.string.tab_profile), Modifier.weight(1f), onLongClick = onLongPressProfile)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BottomBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    badge: Int? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val background by animateColorAsState(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, tween(220), label = "bg")
    val content by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        tween(220), label = "fg",
    )
    Box(
        modifier.fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(background)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick, role = Role.Tab),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
                if (badge != null) {
                    Box(
                        Modifier.align(Alignment.TopEnd).offset(x = 11.dp, y = (-6).dp).size(17.dp)
                            .clip(CircleShape).background(MaterialTheme.colorScheme.error),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(if (badge > 99) "99+" else badge.toString(), color = MaterialTheme.colorScheme.onError, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = content, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
        }
    }
}
