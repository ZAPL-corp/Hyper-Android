package ru.hyper.messenger.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ru.hyper.messenger.data.AuthViewModel
import androidx.compose.ui.res.stringResource
import ru.hyper.messenger.ui.screens.LoginScreen
import ru.hyper.messenger.ui.screens.MainScreen
import ru.hyper.messenger.ui.screens.TwoFactorScreen
import ru.hyper.messenger.ui.screens.WebPageScreen
import ru.hyper.messenger.ui.screens.WelcomeScreen
import ru.hyper.messenger.ui.screens.chats.ChatDetailScreen
import ru.hyper.messenger.ui.screens.profile.EditProfileScreen
import ru.hyper.messenger.ui.screens.settings.NotificationsScreen
import ru.hyper.messenger.ui.screens.settings.PrivacyScreen
import ru.hyper.messenger.ui.screens.settings.QrAuthScreen
import ru.hyper.messenger.ui.screens.settings.ThemeScreen

private object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val TWO_FACTOR = "twofactor/{login}"
    const val REGISTER = "register"
    const val WEB_SETTINGS = "web-settings"
    const val WEB_LOGIN = "web-login"
    const val MAIN = "main"
    const val CHAT = "chat/{chatId}"
    const val THEMES = "themes"
    const val QR = "qr"
    const val NOTIFICATIONS = "notifications"
    const val PRIVACY = "privacy"
    const val EDIT_PROFILE = "edit-profile"

    fun twoFactor(login: String): String = "twofactor/${Uri.encode(login)}"
    fun chat(chatId: String): String = "chat/${Uri.encode(chatId)}"
}

@Composable
fun HyperRoot(container: AppContainer) {
    val navController = rememberNavController()
    val currentUser by container.auth.user.collectAsStateWithLifecycle()
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.factory(container.auth),
    )
    val startDestination = remember {
        if (container.auth.user.value != null) Routes.MAIN else Routes.WELCOME
    }
    var firstUserEmission by remember { mutableStateOf(true) }

    LaunchedEffect(currentUser?.id) {
        if (firstUserEmission) {
            firstUserEmission = false
            return@LaunchedEffect
        }

        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (currentUser != null && currentRoute != Routes.MAIN) {
            navController.navigate(Routes.MAIN) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (currentUser == null && currentRoute != Routes.WELCOME) {
            navController.navigate(Routes.WELCOME) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    CompositionLocalProvider(LocalAppContainer provides container) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
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
                    onBack = { navController.popBackStack() },
                    onRegister = { navController.navigate(Routes.REGISTER) },
                    onRequiresTwoFactor = { login ->
                        navController.navigate(Routes.twoFactor(login))
                    },
                )
            }

            composable(
                route = Routes.TWO_FACTOR,
                arguments = listOf(navArgument("login") { type = NavType.StringType }),
            ) { entry ->
                val login = entry.arguments?.getString("login").orEmpty()
                TwoFactorScreen(
                    login = login,
                    viewModel = authViewModel,
                    onBack = {
                        authViewModel.clearError()
                        navController.popBackStack()
                    },
                )
            }

            composable(Routes.REGISTER) {
                WebPageScreen(
                    url = HyperConfig.REGISTER_URL,
                    title = stringResource(R.string.web_register_title),
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.WEB_SETTINGS) {
                WebPageScreen(
                    url = HyperConfig.SETTINGS_URL,
                    title = stringResource(R.string.web_settings_title),
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.WEB_LOGIN) {
                WebPageScreen(
                    url = HyperConfig.LOGIN_URL,
                    title = stringResource(R.string.web_login_title),
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.MAIN) {
                MainScreen(
                    onOpenChat = { chatId -> navController.navigate(Routes.chat(chatId)) },
                    onOpenThemes = { navController.navigate(Routes.THEMES) },
                    onOpenQr = { navController.navigate(Routes.QR) },
                    onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                    onOpenPrivacy = { navController.navigate(Routes.PRIVACY) },
                    onOpenWebSettings = { navController.navigate(Routes.WEB_SETTINGS) },
                    onOpenEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                )
            }

            composable(
                route = Routes.CHAT,
                arguments = listOf(navArgument("chatId") { type = NavType.StringType }),
            ) { entry ->
                val chatId = entry.arguments?.getString("chatId").orEmpty()
                ChatDetailScreen(
                    chatId = chatId,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.THEMES) {
                ThemeScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.QR) {
                QrAuthScreen(
                    onBack = { navController.popBackStack() },
                    onOpenLoginPage = { navController.navigate(Routes.WEB_LOGIN) },
                )
            }

            composable(Routes.NOTIFICATIONS) {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.PRIVACY) {
                PrivacyScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.EDIT_PROFILE) {
                EditProfileScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
