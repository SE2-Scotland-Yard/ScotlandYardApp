package at.aau.serg.websocketbrokerdemo.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R


@Composable
fun AuthStartScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onRulesClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.start_screen),
            contentDescription = "Start Screen Hintergrund",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = onRulesClick,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .size(80.dp)

        ){
            Icon(
                painter = painterResource(id = R.drawable.rulebutton),
                contentDescription = "Regeln",
                modifier = Modifier.size(80.dp),
                tint = Color.Unspecified
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 160.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Button(
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.buttonStartScreen)
                ),
                modifier = Modifier
                    .width(150.dp)
                    .height(50.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp))
            ) {
                Text("Login", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRegisterClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.buttonStartScreen)
                ),
                modifier = Modifier
                    .width(150.dp)
                    .height(50.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp))
            ) {
                Text("Registrieren", fontSize = 18.sp)
            }
        }
    }
}
