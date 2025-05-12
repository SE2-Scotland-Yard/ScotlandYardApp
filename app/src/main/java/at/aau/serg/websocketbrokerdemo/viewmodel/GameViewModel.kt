import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.websocketbrokerdemo.repository.GameRepository
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository = GameRepository()
) : ViewModel() {

    // Speichert die kompletten Bewegungsdaten (Zielposition + Transportmittel)
    var allowedMoves by mutableStateOf<List<Pair<Int, String>>>(emptyList())
        private set

    // Nur die Zielpositionen (falls du sie separat brauchst)
    val allowedMovePositions: List<Int>
        get() = allowedMoves.map { it.first }

    var message by mutableStateOf("")
        private set


    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun move(gameId: String, name: String, to: Int, gotTicket: String) {
        viewModelScope.launch {
            try {
                message = repository.move(gameId, name, to, gotTicket)
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    fun fetchAllowedMoves(gameId: String, name: String) {
        viewModelScope.launch {
            try {
                allowedMoves = repository.getAllowedMoves(gameId, name)
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = e.message
                allowedMoves = emptyList()
            }
        }
    }

}
