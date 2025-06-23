
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.websocketbrokerdemo.data.model.AllowedMoveResponse
import at.aau.serg.websocketbrokerdemo.repository.GameRepository
import at.aau.serg.websocketbrokerdemo.websocket.StompManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GameViewModel(
    val repository: GameRepository = GameRepository(),
    context: Context
) : ViewModel() {

    var message by mutableStateOf("")
        private set


    var mrXPosition :Int? by mutableStateOf(null)

    var allowedMoves: List<AllowedMoveResponse> by mutableStateOf(emptyList())

    var allowedDoubleMoves: List<AllowedMoveResponse> by mutableStateOf(emptyList())

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val pointPositions: Map<Int, Pair<Int, Int>> = repository.getPointPositions(context)

    var scale : Float by mutableFloatStateOf(1.2f)

    var offsetX by mutableFloatStateOf(0f)
    var offsetY by mutableFloatStateOf(0f)

    var selectedStation : Int by mutableIntStateOf(0)

    var isDoubleMoveMode by mutableStateOf(false)
    var isBlackMoveMode by mutableStateOf(false)

    private val stompManager = StompManager()

    fun updateDoubleMoveMode(enabled: Boolean) {
        isDoubleMoveMode = enabled
    }

    fun move(gameId: String, name: String, to: Int, gotTicket: String) {
        viewModelScope.launch {
            try {

                message = repository.move(gameId, name, to, gotTicket)
            }catch (e:Exception){
                errorMessage = e.message
            }
        }
    }

    fun doubleMove(gameId: String, name: String, to: Int, gotTicket: String) {
        viewModelScope.launch {
            try {
                // Hier rufen wir die spezielle Double-Move API auf

                message = repository.moveDouble(gameId, name, to, gotTicket)

            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    fun blackMove(gameId: String, name: String, to: Int, gotTicket: String) {
        viewModelScope.launch {
            try {

                message = repository.blackMove(gameId, name, to, gotTicket)
            }catch (e:Exception){
                errorMessage = e.message
            }
        }
    }

    fun fetchAllowedMoves(gameId: String, name: String) {
        viewModelScope.launch {
            try {
                allowedMoves = repository.getAllowedMoves(gameId, name)
            } catch (e: Exception) {
                errorMessage = "Fehler: ${e.message}"
            }
        }
    }

    fun fetchMrXPosition(gameId: String, name: String) {
        viewModelScope.launch {
            try {
                mrXPosition = repository.getMrXPosition(gameId, name)
            } catch (e: Exception) {
                errorMessage = "Fehler: ${e.message}"
            }
        }
    }

    fun fetchAllowedDoubleMoves(gameId: String, name: String) {
        viewModelScope.launch {
            try {
                allowedDoubleMoves = repository.getAllowedDoubleMoves(gameId, name)
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    fun fetchMrXHistory(gameId: String, onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.getMrXHistory(gameId)
                onResult(result)
            } catch (e: Exception) {
                errorMessage = "Fehler beim Laden des MrX-Verlaufs: ${e.message}"
            }
        }
    }

    fun resetMoveModes() {
        isBlackMoveMode = false
        isDoubleMoveMode = false
    }

    fun leaveGame(gameId: String, playerId: String, onLeft: () -> Unit) {
        viewModelScope.launch {
            val success = repository.leaveGame(gameId, playerId)
            if (success) {
                stompManager.disconnect()
                withContext(Dispatchers.Main) {
                    onLeft()
                }
            } else {
                Log.e("LEAVE", "Spiel verlassen fehlgeschlagen")
            }
        }
    }

}