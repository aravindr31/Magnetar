package com.northlight.feature.details.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.northlight.feature.details.DetailsScreen
import com.northlight.ui.navigation.Routes

fun NavGraphBuilder.addDetailsGraph(
    navController: NavController,
    onPlayClick: (movieId: Int, torrentHash: String) -> Unit,
    onMovieClick: (Int) -> Unit
) {
    composable(
        route = Routes.DETAILS,
        arguments = listOf(
            navArgument("movieId") { type = NavType.IntType }
        )
    ) {
        DetailsScreen(
            onPlayClick = onPlayClick,
            onMovieClick = onMovieClick,
            onBackClick = { navController.popBackStack() }
        )
    }
}
