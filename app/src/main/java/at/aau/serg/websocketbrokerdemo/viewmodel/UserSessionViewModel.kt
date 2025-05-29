package at.aau.serg.websocketbrokerdemo.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class UserSessionViewModel : ViewModel() {
    var username = mutableStateOf<String?>(null)
    var role = mutableStateOf<String?>(null)

    var avatarResId: Int? = null

    val avatars = mutableStateMapOf<String, Int>()
    val roles = mutableStateMapOf<String, String>()

}
