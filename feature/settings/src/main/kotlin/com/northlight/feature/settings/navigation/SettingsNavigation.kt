package com.northlight.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.northlight.feature.settings.SettingsScreen
import com.northlight.ui.navigation.Routes

fun NavGraphBuilder.addSettingsGraph(
    navController: NavController
) {
    composable(Routes.SETTINGS) {
        SettingsScreen(
            onBackClick = { navController.popBackStack() }
        )
    }
}

