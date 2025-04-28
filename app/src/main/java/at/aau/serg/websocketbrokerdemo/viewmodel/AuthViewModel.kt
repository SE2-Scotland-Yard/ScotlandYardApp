package at.aau.serg.websocketbrokerdemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.websocketbrokerdemo.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    var message: String = ""

    fun login(username: String, password: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.login(username, password)
            message = result
            onResult(result)
        }
    }

    fun register(username: String, password: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.register(username, password)
            message = result
            onResult(result)
        }
    }
}
