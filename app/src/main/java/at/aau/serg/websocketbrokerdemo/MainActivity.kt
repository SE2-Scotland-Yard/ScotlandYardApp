package at.aau.serg.websocketbrokerdemo

import WebSocketClient
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.R

class MainActivity : ComponentActivity(), Callbacks {
    lateinit var client:WebSocketClient
    lateinit var  response:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        client=WebSocketClient(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_fullscreen)

        findViewById<Button>(R.id.connectbtn).setOnClickListener { client.connect() }
        findViewById<Button>(R.id.hellobtn).setOnClickListener{client.sendMessage("Hello")}
        findViewById<Button>(R.id.disconnectbt).setOnClickListener{client.disconnect()}
        response=findViewById(R.id.response_view)

    }

    override fun onResponse(res: String) {
        response.setText(res)
    }


}

