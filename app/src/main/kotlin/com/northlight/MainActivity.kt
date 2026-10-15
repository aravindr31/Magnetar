package com.northlight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.northlight.ui.theme.NorthLightTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NorthLightTheme {
                NorthLightNavHost()
            }
        }
    }
}

@Composable
fun NorthLightNavHost() {
    // Navigation graph wired in feature/browse as the start destination
    // Will be expanded as features are added
    com.northlight.feature.browse.navigation.BrowseNavigation()
}
