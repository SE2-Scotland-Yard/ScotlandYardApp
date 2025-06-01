
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.websocketbrokerdemo.data.model.AllowedMoveResponse
import at.aau.serg.websocketbrokerdemo.repository.GameRepository
import at.aau.serg.websocketbrokerdemo.viewmodel.Ticket
import at.aau.serg.websocketbrokerdemo.functions.CoordinateLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class GameViewModel(
    private val repository: GameRepository = GameRepository(),
    context: Context
) : ViewModel() {

    var message by mutableStateOf("")
        private set

    val shakeDirection = MutableLiveData<String>()

    val currentPlayerPosition = MutableLiveData<Int>()

    var mrXPosition :Int? by mutableStateOf(null)

    var allowedMovesDetails:AllowedMoveResponse? by mutableStateOf(null)

    var allowedMoves: List<AllowedMoveResponse> by mutableStateOf(emptyList())

    var allowedDoubleMoves: List<AllowedMoveResponse> by mutableStateOf(emptyList())

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

    fun move(gameId: String, name: String, to: Int, gotTicket: String, context: Context) {
        viewModelScope.launch {
            try {

                message = repository.move(gameId, name, to, gotTicket)
                vibrate(context)
                currentPlayerPosition.value = to
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

    fun blackMove(gameId: String, name: String, to: Int, gotTicket: String, context: Context) {
        viewModelScope.launch {
            try {

                message = repository.blackMove(gameId, name, to, gotTicket)
                vibrate(context)
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
    /*fun moveDouble(
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
    }*/

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

    fun onShakeDetected(context: Context, currentField: Int, gameId: String, name: String) {
        viewModelScope.launch {
            val mrXField = repository.shakeAndGetMrXPosition(gameId, name)
            if (mrXField == -1) return@launch

            val direction = calculateDirection(context, currentField, mrXField)
            shakeDirection.value = direction
            delay(3000)
            shakeDirection.value = ""
        }
    }

    fun calculateDirection(context: Context, currentFieldId: Int, mrXFieldId: Int): String {
        val coordinates = CoordinateLoader.load(context)
        val from = coordinates[currentFieldId] ?: return ""
        val to = coordinates[mrXFieldId] ?: return ""

        val dx = to.x - from.x
        val dy = to.y - from.y

        return when {
            dx > 0 && dy < 0 -> "Nordost"
            dx < 0 && dy < 0 -> "Nordwest"
            dx > 0 && dy > 0 -> "Südost"
            dx < 0 && dy > 0 -> "Südwest"
            dx == 0 && dy < 0 -> "Norden"
            dx == 0 && dy > 0 -> "Süden"
            dy == 0 && dx > 0 -> "Osten"
            dy == 0 && dx < 0 -> "Westen"
            else -> "Unbekannt"
        }
    }

    private fun vibrate(context: Context, durationMs: Long = 100) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(durationMs)
            }
        }
    }
}