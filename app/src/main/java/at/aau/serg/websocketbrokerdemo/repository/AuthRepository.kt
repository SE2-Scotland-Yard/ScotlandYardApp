package at.aau.serg.websocketbrokerdemo.repository

import at.aau.serg.websocketbrokerdemo.data.api.AuthApi
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class AuthRepository(
    private val api: AuthApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/") // Emulator → localhost
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(AuthApi::class.java)
) {
    suspend fun login(username: String, password: String): String {
        return api.login(username, password)
    }

    suspend fun register(username: String, password: String): String {
        return api.register(username, password)
    }
}
