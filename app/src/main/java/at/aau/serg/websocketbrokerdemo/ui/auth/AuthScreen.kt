package at.aau.serg.websocketbrokerdemo.ui.auth

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import at.aau.serg.websocketbrokerdemo.viewmodel.AuthViewModel
import at.aau.serg.websocketbrokerdemo.viewmodel.UserSessionViewModel
import com.example.myapplication.R


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AuthScreen(
    mode: String = "login",
    onSuccess: () -> Unit = {},
    onBack: () -> Unit = {},
    userSession: UserSessionViewModel
) {
    val context = LocalContext.current
    val authViewModel = remember { AuthViewModel() }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isLogin = mode == "login"

    // Für Enter-Fokuswechsel
    val passwordFocusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.background2),
            contentDescription = "Login Hintergrund",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
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
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { if (it.length <= 20) username = it },
                label = { Text("Benutzername") },
                singleLine = true,
                modifier = Modifier
                    .width(320.dp),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = {
                        passwordFocusRequester.requestFocus()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.85f),
                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { if (it.length <= 20) password = it },
                label = { Text("Passwort") },
                singleLine = true,
                modifier = Modifier
                    .width(320.dp)
                    .focusRequester(passwordFocusRequester),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        if (username.isNotBlank() && password.isNotBlank()) {
                            handleAuth(isLogin, username, password, authViewModel, userSession, context) {
                                onSuccess()
                            }
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.85f),
                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    handleAuth(
                        isLogin,
                        username,
                        password,
                        authViewModel,
                        userSession,
                        context,
                        onSuccess = { result ->
                            onSuccess()
                        }
                    )


                },
                enabled = username.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.width(320.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.buttonStartScreen),
                    contentColor = Color.White.copy(alpha = 0.85f)
                )
            ) {
                Text(if (isLogin) "Einloggen" else "Registrieren")
            }
        }
    }
}

private fun handleAuth(
    isLogin: Boolean,
    username: String,
    password: String,
    authViewModel: AuthViewModel,
    userSession: UserSessionViewModel,
    context: Context,
    onSuccess: (Any?) -> Unit
) {
    if (isLogin) {
        authViewModel.login(username, password) { result ->
            val feedback = result.trim()
            Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
            if (feedback.equals("Login successful", ignoreCase = true)) {
                userSession.username.value = username
                onSuccess(feedback)
            }
        }
    } else {
        authViewModel.register(username, password) { result ->
            val feedback = result.trim()
            Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
            if (feedback.equals("Registration successful", ignoreCase = true)) {
                userSession.username.value = username
                onSuccess(feedback)
            }
        }
    }
}

