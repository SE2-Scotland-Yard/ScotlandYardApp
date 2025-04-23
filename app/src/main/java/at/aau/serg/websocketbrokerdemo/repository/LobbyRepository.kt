import at.aau.serg.websocketbrokerdemo.data.api.LobbyApi
import at.aau.serg.websocketbrokerdemo.data.model.LobbyState
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LobbyRepository(
    private val api: LobbyApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/")
        .addConverterFactory(GsonConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(LobbyApi::class.java)
) {
    suspend fun createLobby(isPublic: Boolean,name: String): LobbyState {
        return api.createLobby(isPublic,name)
    }

    suspend fun joinLobby(gameId: String, playerName: String): Result<String> {
        return try {
            val response = api.joinLobby(gameId, playerName)
            if (response.isSuccessful) {
                val bodyText = response.body()?.string() ?: "Beigetreten"
                Result.success(bodyText)
            } else {
                val errorText = response.errorBody()?.string() ?: "Unbekannter Fehler"
                Result.failure(Exception(errorText))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    suspend fun leaveLobby(gameId: String, name: String): String {
        return api.leaveLobby(gameId, name)
    }

    suspend fun getPublicLobbies(): List<LobbyState> {
        return api.getPublicLobbies()
    }

    suspend fun getLobbyStatus(gameId: String): LobbyState {
        return api.getLobbyStatus(gameId)
    }
}
