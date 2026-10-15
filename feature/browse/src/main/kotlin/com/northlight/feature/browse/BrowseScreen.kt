package com.northlight.feature.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.FilterChip
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.northlight.ui.components.ErrorScreen
import com.northlight.ui.components.LoadingScreen
import com.northlight.ui.components.MovieCard

private val GENRES = listOf(
    "All", "Action", "Adventure", "Animation", "Comedy", "Crime",
    "Documentary", "Drama", "Family", "Fantasy", "Horror", "Mystery",
    "Romance", "Sci-Fi", "Thriller", "War", "Western"
)

private val SORTS = listOf(
    "date_added" to "Latest",
    "rating" to "Top Rated",
    "seeds" to "Most Popular",
    "year" to "Year"
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BrowseScreen(
    onMovieClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BrowseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()

    // Detect when user scrolls near the end to trigger pagination
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItemIndex >= totalItems - 6
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMore()
        }
    }

    if (uiState.isLoading && uiState.movies.isEmpty()) {
        LoadingScreen(message = "Discovering movies...")
        return
    }

    if (uiState.error != null && uiState.movies.isEmpty()) {
        ErrorScreen(
            message = uiState.error ?: "Unable to load movies",
            onRetry = { viewModel.loadMovies(1) }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 32.dp, end = 32.dp)
    ) {
        // Top Header: App Title + Search & Settings Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NorthLight",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Stream YTS Movies on Android TV",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                Button(
                    onClick = onSearchClick,
                    colors = ButtonDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("🔍 Search")
                }
                Spacer(Modifier.width(16.dp))
                Button(
                    onClick = onSettingsClick,
                    colors = ButtonDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("⚙ Settings")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Genre Filter Bar
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(GENRES) { genre ->
                val isSelected = if (genre == "All") {
                    uiState.selectedGenre == null
                } else {
                    uiState.selectedGenre == genre
                }

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.setGenre(if (genre == "All") null else genre)
                    }
                ) {
                    Text(genre)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Sort Options Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(SORTS) { (sortKey, sortLabel) ->
                val isSelected = uiState.sortBy == sortKey
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setSortBy(sortKey) }
                ) {
                    Text(sortLabel)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Movie Poster Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            state = gridState,
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(
                items = uiState.movies,
                key = { _, movie -> movie.id }
            ) { _, movie ->
                MovieCard(
                    title = movie.title,
                    year = movie.year,
                    posterUrl = movie.mediumCoverImage.ifEmpty { movie.largeCoverImage },
                    rating = movie.rating,
                    onClick = { onMovieClick(movie.id) }
                )
            }
        }
    }
}

