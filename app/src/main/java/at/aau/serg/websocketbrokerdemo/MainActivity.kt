package at.aau.serg.websocketbrokerdemo

import WebSocketClient
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.myapplication.R
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity(), Callbacks {
    lateinit var client: WebSocketClient
    var response by mutableStateOf("ResponseText")
    override fun onCreate(savedInstanceState: Bundle?) {
        client = WebSocketClient(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        /*setContentView(R.layout.fragment_fullscreen)

        findViewById<Button>(R.id.connectbtn).setOnClickListener { client.connect() }
        findViewById<Button>(R.id.hellobtn).setOnClickListener{client.sendMessage("Hello")}
        findViewById<Button>(R.id.disconnectbt).setOnClickListener{client.disconnect()}
        response=findViewById(R.id.response_view)
        */

        setContent {
            Connect()
        }

    }


    override fun onResponse(res: String) {
        response = res
    }


    @Composable
    fun Connect() {

        Column(
            modifier = Modifier
                .fillMaxSize()

        ) {
            Button(onClick = { client.connect() }) {
                Text(text = "Connect")
            }
            Button(onClick = { client.sendMessage("Hello") }) {
                Text(text = "Send Message Hello")
            }
            Button(onClick = { client.disconnect() }) {
                Text(text = "Disconnect")
            }
            Text(text = response)

        }


    }
    @Preview(showBackground = true)
    @Composable
    fun ConnectPreview()
    {
        Connect()
    }
}

