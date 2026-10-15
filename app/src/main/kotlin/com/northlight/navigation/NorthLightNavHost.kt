package com.northlight.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.northlight.feature.browse.navigation.addBrowseGraph
import com.northlight.feature.details.navigation.addDetailsGraph
import com.northlight.feature.player.navigation.addPlayerGraph
import com.northlight.feature.settings.navigation.addSettingsGraph
import com.northlight.ui.navigation.Routes

@Composable
fun NorthLightNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.BROWSE,
        modifier = modifier
    ) {
        // Browse & Search Screen
        addBrowseGraph(
            navController = navController,
            onMovieClick = { movieId ->
                navController.navigate(Routes.details(movieId))
            },
            onSettingsClick = {
                navController.navigate(Routes.SETTINGS)
            }
        )

        // Movie Details Screen
        addDetailsGraph(
            navController = navController,
            onPlayClick = { movieId, torrentHash ->
                navController.navigate(Routes.player(movieId, torrentHash))
            },
            onMovieClick = { movieId ->
                navController.navigate(Routes.details(movieId))
            }
        )

        // Player & VLC Stream Screen
        addPlayerGraph(
            navController = navController
        )

        // Settings Screen (Provider switcher)
        addSettingsGraph(
            navController = navController
        )
    }
}
