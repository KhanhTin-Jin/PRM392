// com/example/personaldiary/ui/AppTheme.kt
package com.example.personaldiary.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.personaldiary.core.prefs.SettingsManager

@Composable
fun AppTheme(settings: SettingsManager, content: @Composable () -> Unit) {
    val theme by settings.theme.collectAsState()
    val font by settings.fontSize.collectAsState()

    val dark = theme == "dark"
    val base = if (dark) darkColorScheme() else lightColorScheme()

    // Map font S/M/L → tăng giảm body/title một chút
    val typo = when (font) {
        "s" -> Typography(
            bodyMedium = TextStyle(fontSize = 14.sp),
            titleMedium = TextStyle(fontSize = 18.sp)
        )
        "l" -> Typography(
            bodyMedium = TextStyle(fontSize = 18.sp),
            titleMedium = TextStyle(fontSize = 22.sp)
        )
        else -> Typography( // "m"
            bodyMedium = TextStyle(fontSize = 16.sp),
            titleMedium = TextStyle(fontSize = 20.sp)
        )
    }

    MaterialTheme(colorScheme = base, typography = typo, content = content)
}
