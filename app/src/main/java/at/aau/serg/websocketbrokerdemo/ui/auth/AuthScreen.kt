package at.aau.serg.websocketbrokerdemo.ui.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel


@Composable
fun AuthScreen(mode: String = "login", onSuccess: () -> Unit = {},onBack: () -> Unit = {},  userSession: UserSessionViewModel) {

    val context = LocalContext.current
    val authViewModel = remember { AuthViewModel() }
    var feedback by remember { mutableStateOf("") }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val message by remember { mutableStateOf("") }

    val isLogin = mode == "login"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Zurück"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isLogin) "Login" else "Registrieren",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Benutzername") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Passwort") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                Log.d("AuthButton", if (isLogin) "Login-Button geklickt" else "Register-Button geklickt")
                if (isLogin) {
                    authViewModel.login(username, password) { result ->
                        feedback = result.trim() // Trim zur Sicherheit
                        Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
                        if (feedback.equals("Login successful", ignoreCase = true)) {
                            userSession.username.value = username
                            onSuccess()
                        }
                    }
                } else {
                    authViewModel.register(username, password) { result ->
                        feedback = result.trim()
                        Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
                        if (feedback.equals("Registration successful", ignoreCase = true)) {
                            userSession.username.value = username
                            onSuccess()
                        }
                    }
                }
            },
            enabled = username.isNotBlank() && password.isNotBlank()
        ) {
            Text(if (isLogin) "Einloggen" else "Registrieren")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = MaterialTheme.colorScheme.secondary)
    }
}
