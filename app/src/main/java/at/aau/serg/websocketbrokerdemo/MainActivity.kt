package at.aau.serg.websocketbrokerdemo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import at.aau.serg.websocketbrokerdemo.ui.auth.AuthActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)


        finish()
    }
}
