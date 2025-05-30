import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.websocketbrokerdemo.data.model.AllowedMoveResponse
import at.aau.serg.websocketbrokerdemo.data.model.MrXDoubleMoveResponse
import at.aau.serg.websocketbrokerdemo.repository.GameRepository
import at.aau.serg.websocketbrokerdemo.viewmodel.Ticket
import kotlinx.coroutines.launch




class GameViewModel(
    private val repository: GameRepository = GameRepository(),
    context: Context
) : ViewModel() {

    var message by mutableStateOf("")
        private set


    var mrXPosition :Int? by mutableStateOf(null)

    var allowedMovesDetails:AllowedMoveResponse? by mutableStateOf(null)

    var allowedMoves: List<AllowedMoveResponse> by mutableStateOf(emptyList())

    var allowedDoubleMoves: List<MrXDoubleMoveResponse> by mutableStateOf(emptyList())

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val pointPositions: Map<Int, Pair<Int, Int>> = repository.getPointPositions(context)

    var scale : Float by mutableFloatStateOf(1f)

    var selectedTicket : Ticket? by mutableStateOf(null)

    var selectedStation : Int by mutableIntStateOf(0)

    var isDoubleMoveMode by mutableStateOf(false)
    var isBlackMoveMode by mutableStateOf(false)

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
    fun moveDouble(
        gameId: String,
        name: String,
        firstTo: Int,
        firstTicket: String,
        secondTo: Int,
        secondTicket: String
    ) {
        viewModelScope.launch {
            try {
                val response = repository.moveDouble(gameId, name, firstTo, firstTicket, secondTo, secondTicket)
                message = response.message
            } catch (e: Exception) {
                errorMessage = e.message
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

    fun increaseZoom(){
        scale += 0.1f
    }

    fun decreaseZoom(){
        scale -= 0.1f
    }

    var onZoomChanged: ((Float, Float) -> Unit)? = null

    fun increaseZoom(playerX: Float, playerY: Float) {
        scale = (scale + 0.1f).coerceIn(0.5f, 3f)
        onZoomChanged?.invoke(playerX, playerY)
    }

    fun decreaseZoom(playerX: Float, playerY: Float) {
        scale = (scale - 0.1f).coerceIn(0.5f, 3f)
        onZoomChanged?.invoke(playerX, playerY)
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

}
