package at.aau.serg.websocketbrokerdemo.ui.lobby

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

@Composable
fun MrXSurrenderedOverlay(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .background(
                    color = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mrx),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mr. X hat sich gestellt!(verlassen)",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "🚨 Mr. X hat sich freiwillig bei der nächsten Polizeiwache gestellt. Die Detectives haben gewonnen!",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF4CAF50)
                ),
                modifier = Modifier.width(150.dp)
            ) {
                Text("Zur Lobby")
            }
        }
    }
}
