package at.aau.serg.websocketbrokerdemo.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
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
        return if (playerName.startsWith("[BOT]")) {
            R.drawable.bot
        } else {
            Avatar.fromId(avatarIds[playerName] ?: 1)?.drawableRes ?: R.drawable.bear
        }
    }


    fun isMrX(playerName: String): Boolean {
        return roles[playerName] == "MRX"
    }

    fun getMrXName(): String? {
        return roles.entries.firstOrNull { it.value == "MRX" }?.key
    }

    var isOffline = mutableStateOf(false)
        private set

    fun setOffline(value: Boolean) {
        isOffline.value = value
    }

    fun startNetworkMonitor(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                setOffline(false)
            }

            override fun onLost(network: Network) {
                setOffline(true)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }


}
