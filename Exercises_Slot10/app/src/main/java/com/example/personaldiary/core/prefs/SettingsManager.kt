package com.example.personaldiary.core.prefs

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(prefs.getString("theme_mode", "light")!!) // "light"|"dark"
    val theme: StateFlow<String> = _theme

    private val _fontSize = MutableStateFlow(prefs.getString("font_size", "m")!!) // "s"|"m"|"l"
    val fontSize: StateFlow<String> = _fontSize

    fun setTheme(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _theme.value = mode
    }

    fun setFontSize(size: String) {
        prefs.edit().putString("font_size", size).apply()
        _fontSize.value = size
    }
}
