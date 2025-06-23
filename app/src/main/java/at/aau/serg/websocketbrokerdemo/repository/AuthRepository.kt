package at.aau.serg.websocketbrokerdemo.repository

import at.aau.serg.websocketbrokerdemo.data.api.AuthApi
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class AuthRepository(
    private val api: AuthApi = Retrofit.Builder()
        .baseUrl("http://se2-demo.aau.at:53215/") // Emulator → localhost
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(AuthApi::class.java)
) {
    suspend fun login(username: String, password: String): String {
        return try {
            api.login(username, password)
        } catch (e: Exception) {
            // Netzwerk‑ oder Serverfehler
            "Server ist nicht erreichbar. Bitte versuche es später erneut."
        }
    }

    suspend fun register(username: String, password: String): String {
        return try {
            api.register(username, password)
        } catch (e: Exception) {
            "Server ist nicht erreichbar. Bitte versuche es später erneut."
        }
    }
}
