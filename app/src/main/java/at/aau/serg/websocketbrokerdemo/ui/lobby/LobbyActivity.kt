package at.aau.serg.websocketbrokerdemo.ui.lobby

import GameViewModel
import LobbyScreenType
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.websocketbrokerdemo.ui.OfflineStatusHandler
import at.aau.serg.websocketbrokerdemo.viewmodel.LobbyViewModel
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel


class LobbyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val lobbyVm: LobbyViewModel = viewModel()
            val userSessionVm: UserSessionViewModel = viewModel()
            val snackbarHostState = remember { SnackbarHostState() }

            val isOffline by userSessionVm.isOffline

            var currentScreen by remember { mutableStateOf<LobbyScreenType>(LobbyScreenType.Menu) }
            var selectedGameId by remember { mutableStateOf<String?>(null) }

            val usernameFromIntent = intent.getStringExtra("username") ?: ""

            SideEffect {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

            LaunchedEffect(Unit) {
                userSessionVm.username.value = usernameFromIntent
                userSessionVm.startNetworkMonitor(applicationContext)
            }

            LaunchedEffect(isOffline) {
                if (isOffline) {
                    selectedGameId = null
                    currentScreen = LobbyScreenType.Menu
                }
            }


            val createdLobby = lobbyVm.createdLobby.collectAsState().value
            LaunchedEffect(createdLobby) {
                createdLobby?.let {
                    selectedGameId = it.gameId
                    currentScreen = LobbyScreenType.Live
                }
            }




            when (currentScreen) {
                LobbyScreenType.Menu -> OfflineStatusHandler(isOffline = isOffline) {
                    LobbyMenuScreen(
                        userSession = userSessionVm,
                        onCreateLobby = { isPublic ->
                            lobbyVm.createLobby(isPublic, userSessionVm.username.value.orEmpty())
                        },
                        onJoinLobby = { currentScreen = LobbyScreenType.Join },
                        onFindPublicLobbies = { currentScreen = LobbyScreenType.Public }
                    )
                }

                LobbyScreenType.Join -> OfflineStatusHandler(isOffline = isOffline) {
                    JoinLobbyScreen(
                        onJoin = { id ->
                            val username = userSessionVm.username.value.orEmpty()
                            lobbyVm.tryJoinLobby(id, username, this@LobbyActivity) { result ->
                                if (result.isNotBlank()) {
                                    selectedGameId = id
                                    currentScreen = LobbyScreenType.Live
                                } else {
                                    Toast.makeText(
                                        this@LobbyActivity,
                                        "Beitritt fehlgeschlagen",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        onBack = { currentScreen = LobbyScreenType.Menu },
                        snackbarHostState = snackbarHostState
                    )
                }

                LobbyScreenType.Public -> OfflineStatusHandler(isOffline = isOffline) {

                    PublicLobbiesScreen(
                        onSelect = { id ->
                            val username = userSessionVm.username.value.orEmpty()
                            lobbyVm.tryJoinLobby(id, username, this@LobbyActivity) { result ->
                                if (result.isNotBlank()) {
                                    selectedGameId = id
                                    currentScreen = LobbyScreenType.Live
                                } else {
                                    Toast.makeText(
                                        this@LobbyActivity,
                                        "Beitritt fehlgeschlagen",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        },
                        onBack = { currentScreen = LobbyScreenType.Menu },
                        snackbarHostState = snackbarHostState
                    )
                }

                LobbyScreenType.Live -> selectedGameId?.let { id ->
                    OfflineStatusHandler(isOffline = isOffline) {
                        LobbyScreen(
                            gameId = id,
                            lobbyVm = lobbyVm,
                            userSessionVm = userSessionVm,
                            onLeft = { currentScreen = LobbyScreenType.Menu },
                            onGameStarted = {
                                currentScreen = LobbyScreenType.Game
                            }
                        )
                    }
                }

                LobbyScreenType.Game -> selectedGameId?.let { id ->
                    OfflineStatusHandler(isOffline = isOffline) {
                        GameScreen(
                            gameId = id,
                            lobbyVm = lobbyVm,
                            userSessionVm = userSessionVm,
                            gameVm = GameViewModel(context = this)

                        )
                    }
                }
            }
        }
    }

    companion object {
        fun createIntent(context: Context, username: String): Intent {
            return Intent(context, LobbyActivity::class.java).apply {
                putExtra("username", username)
            }
        }
    }
}
