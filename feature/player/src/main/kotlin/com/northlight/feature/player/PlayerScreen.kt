package com.northlight.feature.player

import android.net.Uri
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.northlight.ui.components.ErrorScreen
import com.northlight.ui.components.LoadingScreen

@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Check if VLC is installed
    LaunchedEffect(Unit) {
        viewModel.setVlcAvailable(VlcPlayerHelper.isVlcInstalled(context))
    }

    // Clean up player and streaming resources when user navigates away
    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanup()
        }
    }

    when {
        uiState.isResolving -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F0F0F))
            ) {
                LoadingScreen(
                    message = "${uiState.movieTitle.ifEmpty { "Movie" }}\n${uiState.progressMessage}"
                )

                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(32.dp),
                    colors = ButtonDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("← Cancel")
                }
            }
        }

        uiState.error != null -> {
            ErrorScreen(
                message = uiState.error ?: "Streaming failed",
                onRetry = { viewModel.startStream() }
            )
        }

        uiState.streamUrl != null -> {
            val streamUrl = checkNotNull(uiState.streamUrl)

            // Remember ExoPlayer instance
            val exoPlayer = remember(context, streamUrl) {
                ExoPlayer.Builder(context).build().apply {
                    val mediaItem = MediaItem.fromUri(Uri.parse(streamUrl))
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = true
                }
            }

            DisposableEffect(exoPlayer) {
                onDispose {
                    exoPlayer.release()
                }
            }

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Media3 AndroidView Player
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Top Floating Control Bar for Android TV
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.colors(
                            containerColor = Color(0xCC1A1A2E)
                        )
                    ) {
                        Text("← Back")
                    }

                    // Open in VLC Button
                    Button(
                        onClick = {
                            if (VlcPlayerHelper.isVlcInstalled(context)) {
                                val vlcIntent = VlcPlayerHelper.createVlcIntent(
                                    streamUrl = streamUrl,
                                    title = uiState.movieTitle
                                )
                                exoPlayer.pause()
                                context.startActivity(vlcIntent)
                            } else {
                                Toast.makeText(
                                    context,
                                    "VLC is not installed. Opening Google Play...",
                                    Toast.LENGTH_LONG
                                ).show()
                                try {
                                    context.startActivity(VlcPlayerHelper.createPlayStoreIntent())
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Could not open Google Play Store",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("🚀 Open in VLC")
                    }
                }
            }
        }
    }
}

