package at.aau.serg.websocketbrokerdemo.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.myapplication.R

@UnstableApi
@Composable
fun VideoPlayerComposable(
    videoUri: String,
    modifier: Modifier = Modifier,
    looping: Boolean = true
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUri)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            if (looping) {
                repeatMode = ExoPlayer.REPEAT_MODE_ALL
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = modifier
    )
}
@UnstableApi
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
        VideoPlayerComposable(
            videoUri = "file:///android_asset/ScotlandYardStart2.mp4",  // WICHTIG: MIT `.mp4`-Endung!
            modifier = Modifier.fillMaxWidth()
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
