package at.aau.serg.websocketbrokerdemo.data.api

import at.aau.serg.websocketbrokerdemo.data.model.LobbyState
import retrofit2.http.*

interface LobbyApi {

    @POST("api/lobby/create")
    suspend fun createLobby(
        @Query("isPublic") isPublic: Boolean,
        @Query("name")     name: String
    ): LobbyState

    @POST("api/lobby/{gameId}/join")
    suspend fun joinLobby(
        @Path("gameId") gameId: String,
        @Query("name") playerName: String
    ): String

    @POST("api/lobby/{gameId}/leave")
    suspend fun leaveLobby(
        @Path("gameId") gameId: String,
        @Query("name")   playerName: String
    ): String

    @GET("api/lobby/public")
    suspend fun getPublicLobbies(): List<LobbyState>

    @GET("api/lobby/{gameId}/status")
    suspend fun getLobbyStatus(@Path("gameId") gameId: String): LobbyState
}