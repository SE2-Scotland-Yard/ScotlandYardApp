package at.aau.serg.websocketbrokerdemo.data

object ApiConfig {
    val baseUrl: String
        get() = if (isEmulator()) {
            "http://10.0.2.2:8080/"
        } else {
            "http://127.0.0.1:8080/"
        }

    private fun isEmulator(): Boolean {
        val fingerprint = android.os.Build.FINGERPRINT.lowercase()
        return fingerprint.contains("generic") || fingerprint.contains("emulator")
    }
}
