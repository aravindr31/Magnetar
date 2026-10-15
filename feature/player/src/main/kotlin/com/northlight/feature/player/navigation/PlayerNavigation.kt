package com.northlight.feature.player.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.northlight.feature.player.PlayerScreen
import com.northlight.ui.navigation.Routes

fun NavGraphBuilder.addPlayerGraph(
    navController: NavController
) {
    composable(
        route = Routes.PLAYER,
        arguments = listOf(
            navArgument("movieId") { type = NavType.IntType },
            navArgument("torrentHash") { type = NavType.StringType }
        )
    ) {
        PlayerScreen(
            onBackClick = { navController.popBackStack() }
        )
    }
}
