@file:Suppress("DEPRECATION")

package com.mad.myfitnesstrackingapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- These names are now matched to your Color.kt file ---

// Light Theme Colors
private val Primary = PrimaryBlue
private val Secondary = SecondaryCyan
private val Tertiary = AccentBlue // Corrected from TertiaryBlueGrey

// Dark Theme Colors
private val PrimaryDark = PrimaryBlue_Dark // Corrected from PrimaryLightBlue
private val SecondaryDark = SecondaryCyan_Dark // Corrected from SecondaryLightCyan
private val TertiaryDark = AccentBlue_Dark // Corrected from TertiaryLightBlueGrey


// Define the color schemes
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = TertiaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Tertiary
)

@Composable
fun FitnessAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Updated logic as you requested
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme



            // Set the status bar color to match the dark top of your gradient
            window.statusBarColor = GradientTop.toArgb()

            // which will look good on your new dark status bar
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // This will use your Typography.kt file
        content = content
    )
}

