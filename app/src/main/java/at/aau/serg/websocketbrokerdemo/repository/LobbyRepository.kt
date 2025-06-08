import at.aau.serg.websocketbrokerdemo.data.api.LobbyApi
import at.aau.serg.websocketbrokerdemo.data.model.JoinResponse
import at.aau.serg.websocketbrokerdemo.data.model.LobbyState
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LobbyRepository(
    private val api: LobbyApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(LobbyApi::class.java)
) {
    suspend fun createLobby(isPublic: Boolean, name: String): LobbyState? {
        return try {
            api.createLobby(isPublic, name)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun joinLobby(gameId: String, playerName: String): JoinResponse? {
        return try {
            api.joinLobby(gameId, playerName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getPublicLobbies(): List<LobbyState> {
        return try {
            api.getPublicLobbies()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getLobbyStatus(gameId: String): LobbyState? {
        return try {
            api.getLobbyStatus(gameId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
