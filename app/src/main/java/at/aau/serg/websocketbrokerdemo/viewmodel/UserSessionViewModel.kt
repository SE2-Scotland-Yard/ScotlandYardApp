package at.aau.serg.websocketbrokerdemo.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class UserSessionViewModel : ViewModel() {
    var username = mutableStateOf<String?>(null)
    var role = mutableStateOf<String?>(null)
}
