package ru.hyper.messenger.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.hyper.messenger.AppContainer
import ru.hyper.messenger.R
import ru.hyper.messenger.core.HyperConfig
import ru.hyper.messenger.data.NativeAuthViewModel
import ru.hyper.messenger.ui.screens.CaptchaScreen
import ru.hyper.messenger.ui.screens.EmailCodeScreen
import ru.hyper.messenger.ui.screens.LoginScreen
import ru.hyper.messenger.ui.screens.MainScreen
import ru.hyper.messenger.ui.screens.RegisterScreen
import ru.hyper.messenger.ui.screens.TwoFactorScreen
import ru.hyper.messenger.ui.screens.WebPageScreen
import ru.hyper.messenger.ui.screens.WelcomeScreen
import ru.hyper.messenger.ui.screens.chats.ChatDetailScreen
import ru.hyper.messenger.ui.screens.profile.EditProfileScreen
import ru.hyper.messenger.ui.screens.settings.NotificationsScreen
import ru.hyper.messenger.ui.screens.settings.PrivacyScreen
import ru.hyper.messenger.ui.screens.settings.SecurityScreen
import ru.hyper.messenger.ui.screens.settings.ThemeScreen

private object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VERIFY = "verify"
    const val TWO_FA = "twofactor"
    const val CAPTCHA = "captcha"
    const val WEB_SETTINGS = "web-settings"
    const val MAIN = "main"
    const val CHAT = "chat/{chatId}"
    const val THEMES = "themes"
    const val NOTIFICATIONS = "notifications"
    const val PRIVACY = "privacy"
    const val EDIT_PROFILE = "edit-profile"
    const val SECURITY = "security"
    fun chat(id: String) = "chat/${android.net.Uri.encode(id)}"
}

@Composable
fun HyperRoot(container: AppContainer) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val currentUser by container.auth.user.collectAsStateWithLifecycle()
    val activeAccount by container.auth.activeAccount.collectAsStateWithLifecycle()
    val serverHost by container.preferences.serverHost.collectAsStateWithLifecycle()
    val authViewModel: NativeAuthViewModel = viewModel(factory = NativeAuthViewModel.factory(container.auth))
    val start = remember { if (container.auth.user.value != null) Routes.MAIN else Routes.WELCOME }
    var firstUserEmission by remember { mutableStateOf(true) }
    var showNotificationInfo by remember { mutableStateOf(false) }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(activeAccount) { container.chats.connect(activeAccount) }

    LaunchedEffect(currentUser?.id) {
        if (firstUserEmission) { firstUserEmission = false; return@LaunchedEffect }
        val route = navController.currentBackStackEntry?.destination?.route
        if (currentUser != null && route != Routes.MAIN) {
            navController.navigate(Routes.MAIN) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (currentUser == null && route != Routes.WELCOME) {
            navController.navigate(Routes.WELCOME) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(currentUser?.id) {
        if (currentUser == null) return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted && !container.preferences.notificationPromptShown) {
                container.preferences.notificationPromptShown = true
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            val manager = context.getSystemService(NotificationManager::class.java)
            if (manager?.areNotificationsEnabled() == false && !container.preferences.notificationPromptShown) {
                container.preferences.notificationPromptShown = true
                showNotificationInfo = true
            }
        }
    }

    CompositionLocalProvider(LocalAppContainer provides container) {
        NavHost(
            navController = navController,
            startDestination = start,
            enterTransition = {
                fadeIn(tween(220)) + slideInHorizontally(tween(260, easing = FastOutSlowInEasing)) { it / 10 }
            },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = {
                fadeIn(tween(200)) + slideInHorizontally(tween(240, easing = FastOutSlowInEasing)) { -it / 12 }
            },
            popExitTransition = {
                fadeOut(tween(180)) + slideOutHorizontally(tween(220, easing = FastOutSlowInEasing)) { it / 12 }
            },
        ) {
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onLogin = { navController.navigate(Routes.LOGIN) },
                    onRegister = { navController.navigate(Routes.REGISTER) },
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = authViewModel,
                    onBack = {
                        authViewModel.resetFlow()
                        container.auth.cancelAddAccount()
                        container.auth.clearError()
                        navController.popBackStack()
                    },
                    onRegister = { authViewModel.resetFlow(); navController.navigate(Routes.REGISTER) },
                    onEmailCode = { navController.navigate(Routes.VERIFY) },
                    onTwoFactor = { navController.navigate(Routes.TWO_FA) },
                    onCaptcha = { navController.navigate(Routes.CAPTCHA) },
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onBack = { authViewModel.resetFlow(); navController.popBackStack() },
                    onEmailCode = { navController.navigate(Routes.VERIFY) },
                    onCaptcha = { navController.navigate(Routes.CAPTCHA) },
                )
            }

            composable(Routes.VERIFY) {
                EmailCodeScreen(
                    viewModel = authViewModel,
                    onBack = { authViewModel.resetFlow(); navController.popBackStack() },
                    onTwoFactor = { navController.navigate(Routes.TWO_FA) },
                )
            }

            composable(Routes.TWO_FA) {
                TwoFactorScreen(
                    viewModel = authViewModel,
                    onBack = { authViewModel.backFromTwoFactor(); navController.popBackStack() },
                )
            }

            composable(Routes.CAPTCHA) {
                CaptchaScreen(
                    url = HyperConfig.captchaUrl(serverHost),
                    onBack = { authViewModel.cancelCaptcha(); navController.popBackStack() },
                    onToken = { token ->
                        authViewModel.provideCaptcha(token)
                        navController.popBackStack()
                    },
                )
            }

            composable(Routes.WEB_SETTINGS) {
                WebPageScreen(
                    url = HyperConfig.homeUrl(serverHost),
                    title = stringResource(R.string.web_settings_title),
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.MAIN) {
                MainScreen(
                    onOpenChat = { navController.navigate(Routes.chat(it)) },
                    onOpenThemes = { navController.navigate(Routes.THEMES) },
                    onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                    onOpenPrivacy = { navController.navigate(Routes.PRIVACY) },
                    onOpenWebSettings = { navController.navigate(Routes.WEB_SETTINGS) },
                    onOpenEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                    onOpenSecurity = { navController.navigate(Routes.SECURITY) },
                    onAddAccount = {
                        authViewModel.resetFlow()
                        container.auth.beginAddAccount()
                        navController.navigate(Routes.LOGIN)
                    },
                )
            }

            composable(
                route = Routes.CHAT,
                arguments = listOf(navArgument("chatId") { type = NavType.StringType }),
                enterTransition = {
                    slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(240))
                },
                exitTransition = { fadeOut(tween(160)) },
                popExitTransition = {
                    slideOutHorizontally(tween(280, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(200))
                },
            ) { entry ->
                ChatDetailScreen(entry.arguments?.getString("chatId").orEmpty()) { navController.popBackStack() }
            }

            composable(Routes.THEMES) { ThemeScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.NOTIFICATIONS) { NotificationsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.PRIVACY) { PrivacyScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.EDIT_PROFILE) { EditProfileScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SECURITY) { SecurityScreen(onBack = { navController.popBackStack() }) }
        }
    }

    if (showNotificationInfo) {
        AlertDialog(
            onDismissRequest = { showNotificationInfo = false },
            title = { Text(stringResource(R.string.notifications_permission_title)) },
            text = { Text(stringResource(R.string.notifications_permission_denied)) },
            confirmButton = {
                TextButton(onClick = {
                    showNotificationInfo = false
                    runCatching {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                        )
                    }
                }) { Text(stringResource(R.string.open_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationInfo = false }) { Text(stringResource(R.string.close)) }
            },
        )
    }
}
