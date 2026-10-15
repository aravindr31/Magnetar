package com.northlight.feature.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.FilterChip
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.northlight.ui.components.ErrorScreen
import com.northlight.ui.components.LoadingScreen
import com.northlight.ui.components.MovieCard

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DetailsScreen(
    onPlayClick: (movieId: Int, torrentHash: String) -> Unit,
    onMovieClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading && uiState.ytsMovie == null) {
        LoadingScreen(message = "Loading movie details...")
        return
    }

    if (uiState.error != null && uiState.ytsMovie == null) {
        ErrorScreen(
            message = uiState.error ?: "Failed to load movie",
            onRetry = { viewModel.loadDetails() }
        )
        return
    }

    val yts = uiState.ytsMovie ?: return
    val tmdb = uiState.tmdbMovie

    val backdropUrl = tmdb?.backdropUrl()
        ?: yts.backgroundImageOriginal.ifEmpty { yts.backgroundImage }
    val posterUrl = tmdb?.posterUrl()
        ?: yts.largeCoverImage.ifEmpty { yts.mediumCoverImage }

    Box(modifier = modifier.fillMaxSize()) {
        // High-res Background Image with cinematic gradient
        AsyncImage(
            model = backdropUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient scrim to ensure text legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xF00F0F0F),
                            Color(0xD00F0F0F),
                            Color(0x800F0F0F)
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 32.dp),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            item {
                // Top Navigation Row
                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("← Back")
                }

                Spacer(Modifier.height(24.dp))

                // Hero Row: Poster + Info
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Movie Poster
                    AsyncImage(
                        model = posterUrl,
                        contentDescription = yts.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(200.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(Modifier.width(32.dp))

                    // Movie Meta & Actions
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = yts.title,
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (!tmdb?.tagline.isNullOrBlank()) {
                            Text(
                                text = "\"${tmdb?.tagline}\"",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        // Meta details: Year, Runtime, Rating
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "${yts.year}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (yts.runtime > 0) {
                                Text(
                                    text = "${yts.runtime} min",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "★ ${yts.rating} / 10",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Genre chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(yts.genres) { genre ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = genre,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Overview / Description
                        val overviewText = tmdb?.overview?.ifEmpty { yts.descriptionFull }
                            ?: yts.descriptionFull.ifEmpty { yts.summary }

                        Text(
                            text = overviewText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        // Available Torrent Qualities
                        Text(
                            text = "Select Quality / Stream Source:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(yts.torrents) { torrent ->
                                val isSelected = uiState.selectedTorrent?.hash == torrent.hash
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectTorrent(torrent) }
                                ) {
                                    Text("${torrent.quality} (${torrent.type.uppercase()}) • ${torrent.size} • 🌱 ${torrent.seeds}")
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Play Buttons Row
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            val selectedHash = uiState.selectedTorrent?.hash
                                ?: yts.torrents.firstOrNull()?.hash

                            Button(
                                onClick = {
                                    if (selectedHash != null) {
                                        onPlayClick(yts.id, selectedHash)
                                    }
                                },
                                enabled = selectedHash != null
                            ) {
                                Text("▶  Stream Now (Built-in Server)")
                            }
                        }
                    }
                }
            }

            // Cast section (from TMDB)
            if (!tmdb?.credits?.cast.isNullOrEmpty()) {
                item {
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = "Top Cast",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(tmdb?.credits?.cast?.take(10) ?: emptyList()) { actor ->
                            Card(
                                onClick = {},
                                modifier = Modifier.width(120.dp),
                                colors = CardDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    AsyncImage(
                                        model = actor.profileUrl(),
                                        contentDescription = actor.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    )
                                    Text(
                                        text = actor.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                    Text(
                                        text = actor.character,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Suggestions / Similar Movies
            if (uiState.suggestions.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = "You May Also Like",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(uiState.suggestions) { suggestion ->
                            MovieCard(
                                title = suggestion.title,
                                year = suggestion.year,
                                posterUrl = suggestion.mediumCoverImage.ifEmpty { suggestion.largeCoverImage },
                                rating = suggestion.rating,
                                onClick = { onMovieClick(suggestion.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

