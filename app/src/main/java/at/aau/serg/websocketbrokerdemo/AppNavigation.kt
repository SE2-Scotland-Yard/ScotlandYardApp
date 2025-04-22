package at.aau.serg.websocketbrokerdemo

import GameScreen
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import at.aau.serg.websocketbrokerdemo.ui.auth.AuthScreen
import at.aau.serg.websocketbrokerdemo.ui.lobby.*
import at.aau.serg.websocketbrokerdemo.ui.start.StartScreen
import at.aau.serg.websocketbrokerdemo.viewmodel.*

@Composable
fun AppNavigation() {


    val nav   = rememberNavController()
    val user  : UserSessionViewModel = viewModel()
    val lobby : LobbyViewModel       = viewModel()

    NavHost(nav, startDestination = AppRoutes.START) {

        /* ---------- Start ---------- */
        composable(AppRoutes.START) {
            StartScreen(
                onLoginClick    = { nav.navigate("auth?mode=login") },
                onRegisterClick = { nav.navigate("auth?mode=register") }
            )
        }

        /* ---------- Auth ---------- */
        composable(
            AppRoutes.AUTH,
            arguments = listOf(
                navArgument("mode") { defaultValue = "login"; type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "login"
            AuthScreen(
                mode        = mode,
                userSession = user,
                onSuccess   = { nav.navigate(AppRoutes.MENU) },
                onBack      = { nav.popBackStack() }
            )
        }

        /* ---------- Menü ---------- */
        composable(AppRoutes.MENU) {

            /* Direkt nach dem Erzeugen einer Lobby dorthin springen */
            val created by lobby.createdLobby.collectAsState()
            LaunchedEffect(created) {
                created?.let { nav.navigate(AppRoutes.lobby(it.gameId)) }
            }

            LobbyMenuScreen(
                userSession         = user,
                onCreateLobby       = { pub -> lobby.createLobby(pub, user.username.value.orEmpty()) },
                onJoinLobby         = { nav.navigate(AppRoutes.JOIN)   },
                onFindPublicLobbies = { nav.navigate(AppRoutes.PUBLIC) }
            )
        }

        /* ---------- Join ---------- */
        composable(AppRoutes.JOIN) {
            JoinLobbyScreen(
                onJoin = { id ->
                    lobby.joinLobby(id, user.username.value.orEmpty())
                    nav.navigate(AppRoutes.lobby(id))
                },
                onBack = { nav.popBackStack() }
            )
        }

        /* ---------- Öffentliche Lobbys ---------- */
        composable(AppRoutes.PUBLIC) {
            PublicLobbiesScreen(
                onSelect = { id ->
                    lobby.joinLobby(id, user.username.value.orEmpty())
                    nav.navigate(AppRoutes.lobby(id))
                },
                onBack = { nav.popBackStack() }
            )
        }

        /* ---------- Live‑Lobby ---------- */
        composable(
            route      = AppRoutes.LOBBY,
            arguments  = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments!!.getString("id")!!

            LobbyScreen(
                gameId        = id,
                lobbyVm       = lobby,
                userSessionVm = user,
                onLeft        = {
                    nav.navigate(AppRoutes.MENU) {
                        popUpTo(AppRoutes.MENU) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onGameStarted = {
                    nav.navigate(AppRoutes.game(id)) {
                        launchSingleTop = true
                    }
                }
            )

        }

        /* ---------- Live‑Game ---------- */
        composable(
            route = AppRoutes.GAME,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable

            GameScreen(
                gameId = id,
                userSessionVm = user,
                lobbyVm = lobby,
                onLeft = {
                    nav.navigate(AppRoutes.MENU) {
                        popUpTo(AppRoutes.MENU) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }


    }
}
