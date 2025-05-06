package at.aau.serg.websocketbrokerdemo.ui.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapScreen()
        }
    }

    companion object {

        fun createIntent(context: android.content.Context): android.content.Intent {
            return android.content.Intent(context, GameActivity::class.java)
        }
    }
}