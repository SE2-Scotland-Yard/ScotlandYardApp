package at.aau.serg.websocketbrokerdemo.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import at.aau.serg.websocketbrokerdemo.model.Avatar
import com.example.myapplication.R

class UserSessionViewModel : ViewModel() {
    var username = mutableStateOf<String?>(null)
    var role = mutableStateOf<String?>(null)

    var avatarResId: Int? = null

    val avatarIds = mutableStateMapOf<String, Int>()
    val roles = mutableStateMapOf<String, String>()

    fun getAvatarDrawableRes(playerName: String): Int {
        return Avatar.fromId(avatarIds[playerName] ?: 1)?.drawableRes ?: R.drawable.bear
    }

}
