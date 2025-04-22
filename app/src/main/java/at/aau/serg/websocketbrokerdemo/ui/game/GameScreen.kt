import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import at.aau.serg.websocketbrokerdemo.viewmodel.LobbyViewModel
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel

@Composable
fun GameScreen(
    gameId: String,
    lobbyVm: LobbyViewModel,
    userSessionVm: UserSessionViewModel,
    onLeft: () -> Unit
) {
    Text(text = "Game ID: $gameId")
}
