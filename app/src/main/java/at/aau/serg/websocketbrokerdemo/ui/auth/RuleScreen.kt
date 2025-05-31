package at.aau.serg.websocketbrokerdemo.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale





@Composable
fun RuleScreen(onBack: () -> Unit){
    val ruleImages = listOf(
        R.drawable.scotlandyardrules_1,
        R.drawable.scotlandyardrules_2,
        R.drawable.scotlandyardrules_3,
        R.drawable.scotlandyardrules_4,
        R.drawable.scotlandyardrules_5,
        R.drawable.scotlandyardrules_6,
        R.drawable.scotlandyardrules_7
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.start_screen),
            contentDescription = "Start Screen Hintergrund",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(ruleImages) { imageRes ->
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Rule Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp)
                )
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(80.dp)

        ){
            Icon(
                painter = painterResource(id = R.drawable.exitbutton),
                contentDescription = "ExitButton",
                modifier = Modifier.size(80.dp),
                tint = Color.Unspecified
            )
        }
    }







}

