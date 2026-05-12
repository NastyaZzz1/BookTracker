package com.nastya.booktracker.presentation.ui

import com.nastya.booktracker.data.local.dao.BookSettingsDao
import com.nastya.booktracker.domain.model.BookSettings
import org.readium.r2.navigator.preferences.FontFamily
import org.readium.r2.navigator.preferences.Theme
import org.readium.r2.shared.ExperimentalReadiumApi

@OptIn(ExperimentalReadiumApi::class)
class BookSettingsRepository(
    private val bookSettingsDao: BookSettingsDao
) {
    suspend fun getSettings(): BookSettings? = bookSettingsDao.getSettings()

    suspend fun saveSettings(
        theme: Theme,
        fontSize: Double,
        fontFamily: FontFamily,
        fontIndex: Int
    ) {
        val fontFamilyType = when (fontFamily) {
            FontFamily.SANS_SERIF -> FontFamilyType.SANS_SERIF
            FontFamily.SERIF -> FontFamilyType.SERIF
            FontFamily.MONOSPACE -> FontFamilyType.MONOSPACE
            FontFamily.CURSIVE -> FontFamilyType.CURSIVE
            else -> FontFamilyType.SERIF
        }

        val themeType = when (theme) {
            Theme.LIGHT -> ThemeType.LIGHT
            Theme.SEPIA -> ThemeType.SEPIA
            Theme.DARK -> ThemeType.DARK
        }

        val settings = BookSettings(
            id = 1,
            theme = themeType,
            fontSize = fontSize,
            fontFamily = fontFamilyType,
            fontIndex = fontIndex
        )

        SettingsCache.update(
            theme = themeType,
            fontSize = fontSize,
            fontFamily = fontFamilyType,
            fontIndex = fontIndex
        )

        bookSettingsDao.insert(settings)
    }
}