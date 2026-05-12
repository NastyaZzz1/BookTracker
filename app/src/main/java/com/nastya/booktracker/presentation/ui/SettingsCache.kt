package com.nastya.booktracker.presentation.ui

import org.readium.r2.navigator.preferences.FontFamily
import org.readium.r2.navigator.preferences.Theme
import org.readium.r2.shared.ExperimentalReadiumApi

@OptIn(ExperimentalReadiumApi::class)
object SettingsCache {
    var theme: ThemeType = ThemeType.LIGHT
    var fontSize: Double = 1.0
    var fontFamily: FontFamilyType = FontFamilyType.SERIF
    var fontIndex: Int = 1

    var isLoaded: Boolean = false

    fun update(
        theme: ThemeType = this.theme,
        fontSize: Double = this.fontSize,
        fontFamily: FontFamilyType = this.fontFamily,
        fontIndex: Int = this.fontIndex
    ) {
        this.theme = theme
        this.fontSize = fontSize
        this.fontFamily = fontFamily
        this.fontIndex = fontIndex
    }

    fun reset() {
        theme = ThemeType.LIGHT
        fontSize = 1.0
        fontFamily = FontFamilyType.SERIF
        fontIndex = 1
        isLoaded = false
    }

    fun getReadiumTheme(): Theme {
        return when (theme) {
            ThemeType.LIGHT -> Theme.LIGHT
            ThemeType.SEPIA -> Theme.SEPIA
            ThemeType.DARK -> Theme.DARK
        }
    }

    fun getReadiumFontFamily(): FontFamily {
        return when (fontFamily) {
            FontFamilyType.SANS_SERIF -> FontFamily.SANS_SERIF
            FontFamilyType.SERIF -> FontFamily.SERIF
            FontFamilyType.MONOSPACE -> FontFamily.MONOSPACE
            FontFamilyType.CURSIVE -> FontFamily.CURSIVE
        }
    }
}