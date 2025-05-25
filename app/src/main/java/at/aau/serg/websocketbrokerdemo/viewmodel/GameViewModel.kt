import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.websocketbrokerdemo.data.model.AllowedMoveResponse
import at.aau.serg.websocketbrokerdemo.data.model.MrXDoubleMoveResponse
import at.aau.serg.websocketbrokerdemo.repository.GameRepository
import kotlinx.coroutines.launch




class GameViewModel(
    private val repository: GameRepository = GameRepository()
) : ViewModel() {

    var message by mutableStateOf("")
        private set


    var mrXPosition :Int? by mutableStateOf(null)

    var allowedMovesDetails:AllowedMoveResponse? by mutableStateOf(null)

    var allowedMoves: List<AllowedMoveResponse> by mutableStateOf(emptyList())

    var allowedDoubleMoves: List<MrXDoubleMoveResponse> by mutableStateOf(emptyList())

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isDoubleMoveMode by mutableStateOf(false)

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

}
