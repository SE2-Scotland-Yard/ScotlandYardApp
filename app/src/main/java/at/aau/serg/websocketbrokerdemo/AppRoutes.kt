package at.aau.serg.websocketbrokerdemo

/**
 * Zentrale Route‑Konstanten + Helper‑Funktion zum Erzeugen der Lobby‑Route.
 */
object AppRoutes {
    const val START  = "start"
    const val AUTH   = "auth?mode={mode}"
    const val MENU   = "menu"
    const val JOIN   = "join"
    const val PUBLIC = "public"
    const val LOBBY  = "lobby/{id}"
    const val GAME   = "game/{id}"


    fun lobby(id: String) = "lobby/$id"
    fun game(id: String) = "game/$id"
}
