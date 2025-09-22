package com.example.noteboard.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    secondary = YellowSecondary,
    surface = Color.White,
    background = Color(0xFFFDFDFD),
)

private val DarkColors = darkColorScheme(
    primary = GreenPrimary,
    secondary = YellowSecondary,
    surface = Color(0xFF121212),
    background = Color(0xFF101010),
)

@Composable
fun NoteBoardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        window.statusBarColor = Color.Transparent.value.toInt()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        shapes = Shapes(
            small = RoundedCornerShape(8),
            medium = RoundedCornerShape(16),
            large = RoundedCornerShape(28)
        ),
        content = content
    )
}
