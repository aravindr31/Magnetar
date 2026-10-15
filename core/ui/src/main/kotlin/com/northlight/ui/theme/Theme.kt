package com.northlight.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

private val NorthLightColorScheme = darkColorScheme(
    primary = NorthLightPrimary,
    onPrimary = NorthLightOnPrimary,
    secondary = NorthLightSecondary,
    background = NorthLightBackground,
    onBackground = NorthLightOnBackground,
    surface = NorthLightSurface,
    onSurface = NorthLightOnSurface,
    surfaceVariant = NorthLightSurfaceVariant,
    onSurfaceVariant = NorthLightOnSurfaceVariant,
    error = NorthLightError,
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun NorthLightTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NorthLightColorScheme,
        shapes = MaterialTheme.shapes.copy(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp)
        ),
        content = content
    )
}

