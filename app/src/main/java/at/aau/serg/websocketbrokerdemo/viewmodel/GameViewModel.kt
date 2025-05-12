package at.aau.serg.websocketbrokerdemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.websocketbrokerdemo.repository.GameRepository
import kotlinx.coroutines.launch


class GameViewModel(
    private val repository: GameRepository = GameRepository()
) : ViewModel() {

    var message: String = ""


    fun move(gameId: String, name: String, to: Int, ticket: String) {
            viewModelScope.launch {
                val result = repository.move(gameId, name, to, ticket)
                message = result

            }


    }



}
