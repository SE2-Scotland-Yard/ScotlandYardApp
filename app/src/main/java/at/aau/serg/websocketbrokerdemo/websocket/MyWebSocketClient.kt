import android.os.Handler
import android.os.Looper
import android.util.Log
import at.aau.serg.websocketbrokerdemo.websocket.Callbacks
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

private const val WEBSOCKET_URL = "ws://10.0.2.2:8080/scotlandyard"

class WebSocketClient(val callbacks: Callbacks) {

    private val client = OkHttpClient()
    private lateinit var webSocket: WebSocket

    fun connect() {
        val request = Request.Builder().url(WEBSOCKET_URL).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                callback("Connected to WebSocket")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Message received: $text")
                callback(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                callback("Received binary message")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                callback("Closing WebSocket: $reason")
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                callback("WebSocket Failure: ${t.message}")
                Log.e("WebSocket", "Error: ", t)
            }
        })
    }

    fun sendMessage(message: String) {
        webSocket.send(message)
    }

    fun disconnect() {
        webSocket.close(1000, "Client disconnected")
    }

    private fun callback(msg: String) {
        Handler(Looper.getMainLooper()).post {
            callbacks.onResponse(msg)
        }
    }
}
