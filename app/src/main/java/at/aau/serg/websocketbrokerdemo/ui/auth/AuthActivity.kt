package at.aau.serg.websocketbrokerdemo.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.websocketbrokerdemo.ui.lobby.LobbyActivity
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.SideEffect
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi


class AuthActivity : ComponentActivity() {

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val userSessionViewModel: UserSessionViewModel = viewModel()

            var screenState by remember { mutableStateOf<AuthScreenState>(AuthScreenState.Start) }

            SideEffect {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }



            when (screenState) {
                AuthScreenState.Start -> AuthStartScreen(
                    onLoginClick = { screenState = AuthScreenState.Login },
                    onRegisterClick = { screenState = AuthScreenState.Register },
                    onRulesClick = { screenState = AuthScreenState.Rules }
                )

                AuthScreenState.Login -> AuthScreen(
                    mode = "login",
                    userSession = userSessionViewModel,
                    onSuccess = {
                        startActivity(LobbyActivity.createIntent(this, userSessionViewModel.username.value.orEmpty()))
                        finish()
                    },
                    onBack = { screenState = AuthScreenState.Start }
                )

                AuthScreenState.Register -> AuthScreen(
                    mode = "register",
                    userSession = userSessionViewModel,
                    onSuccess = {
                        startActivity(LobbyActivity.createIntent(this, userSessionViewModel.username.value.orEmpty()))
                        finish()
                    },
                    onBack = { screenState = AuthScreenState.Start }
                )

                AuthScreenState.Rules -> RuleScreen(
                    onBack = { screenState = AuthScreenState.Start }
                )
            }
        }
    }

    companion object {
        fun createIntent(context: Context, username: String): Intent {
            return Intent(context, LobbyActivity::class.java).apply {
                putExtra("username", username)
            }
        }
    }

}


private enum class AuthScreenState {
    Start,
    Login,
    Register,
    Rules
}
