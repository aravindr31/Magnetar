package com.northlight.feature.browse.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.northlight.feature.browse.BrowseScreen
import com.northlight.feature.browse.SearchScreen
import com.northlight.ui.navigation.Routes

fun NavGraphBuilder.addBrowseGraph(
    navController: NavController,
    onMovieClick: (Int) -> Unit,
    onSettingsClick: () -> Unit
) {
    composable(Routes.BROWSE) {
        BrowseScreen(
            onMovieClick = onMovieClick,
            onSearchClick = { navController.navigate(Routes.SEARCH) },
            onSettingsClick = onSettingsClick
        )
    }

    composable(Routes.SEARCH) {
        SearchScreen(
            onMovieClick = onMovieClick,
            onBackClick = { navController.popBackStack() }
        )
    }
}
