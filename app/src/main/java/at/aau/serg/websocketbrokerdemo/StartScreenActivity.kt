package at.aau.serg.websocketbrokerdemo

import android.content.Intent
import android.media.Image
import android.os.Bundle
import androidx.activity.ComponentActivity


import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import at.aau.serg.websocketbrokerdemo.ui.theme.MyApplicationTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.sp
import com.example.myapplication.R



class StartScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StartScreen()
        }
    }
}

@Composable
fun StartScreen() {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {

        Image(
            painter = painterResource(id = R.drawable.start_screen),
            contentDescription = "Scotland Yard Start Screen",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()

        )
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Column(
                modifier = Modifier
                    .offset(x = 180.dp, y = 90.dp)
            ){
                Button(
                    onClick = { /*TODO*/ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.buttonStartScreen,)
                    ),
                    modifier = Modifier
                        .size(width = 150.dp, height = 50.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = Color.Black,
                            spotColor = Color.DarkGray
                        )
                ) {

                    Text(text = "Start" , fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.size(25.dp))

                Button(
                    onClick = { /*TODO*/ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.buttonStartScreen,)
                    ),
                    modifier = Modifier
                        .size(width = 150.dp, height = 50.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = Color.Black,
                            spotColor = Color.DarkGray
                        )
                ) {
                    Text(text = "Settings", fontSize = 20.sp)
                }

                //Temporary Button
                Spacer(modifier = Modifier.size(25.dp))

                Button(
                    onClick = {
                        val intent = Intent(context, MapActivity::class.java)
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.buttonStartScreen,)
                    ),
                    modifier = Modifier
                        .size(width = 150.dp, height = 50.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = Color.Black,
                            spotColor = Color.DarkGray
                        )
                ) {
                    Text(text = "To Map", fontSize = 20.sp)
                }
            }
        }


    }



}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StartScreen()
}