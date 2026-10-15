package com.northlight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.northlight.navigation.NorthLightNavHost
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
