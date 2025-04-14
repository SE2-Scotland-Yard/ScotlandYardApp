package at.aau.serg.websocketbrokerdemo

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import at.aau.serg.websocketbrokerdemo.ui.auth.AuthScreen
import at.aau.serg.websocketbrokerdemo.ui.lobby.LobbyMenuScreen
import at.aau.serg.websocketbrokerdemo.ui.start.StartScreen
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val userSession: UserSessionViewModel = viewModel()


    NavHost(navController = navController, startDestination = "start") {

        composable("start") {
            StartScreen(
                onLoginClick = { navController.navigate("auth?mode=login") },
                onRegisterClick = { navController.navigate("auth?mode=register") }
            )
        }

        composable(
            route = "auth?mode={mode}",
            arguments = listOf(navArgument("mode") {
                defaultValue = "login"
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "login"
            AuthScreen(
                mode = mode,
                onSuccess = { navController.navigate("lobbyMenu") },
                onBack = { navController.popBackStack() },
                userSession = userSession
            )
        }

        composable("lobbyMenu") {
            LobbyMenuScreen(
                onCreateLobby = { /* TODO: navController.navigate("createLobby") */ },
                onJoinLobby = { /* TODO: navController.navigate("joinLobby") */ },
                onFindPublicLobbies = { /* TODO: navController.navigate("publicLobbies") */ },
                userSession = userSession
            )
        }


    }
}
