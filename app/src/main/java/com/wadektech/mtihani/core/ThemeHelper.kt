package com.wadektech.mtihani.core

import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    private const val DARK_MODE = "Dark"

    fun applyTheme(themePreference: String) {
        when (themePreference) {
            DARK_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}