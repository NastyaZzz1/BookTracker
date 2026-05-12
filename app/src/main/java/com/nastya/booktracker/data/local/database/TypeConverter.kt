package com.nastya.booktracker.data.local.database

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.nastya.booktracker.presentation.ui.FontFamilyType
import com.nastya.booktracker.presentation.ui.ThemeType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class TypeConverter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun toString(date: LocalDate?): String? = date?.format(formatter)

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it, formatter) }

    @TypeConverter
    fun fromThemeType(theme: ThemeType): String = theme.name

    @TypeConverter
    fun toThemeType(theme: String): ThemeType = try {
        ThemeType.valueOf(theme)
    } catch (e: IllegalArgumentException) {
        ThemeType.LIGHT
    }

    @TypeConverter
    fun fromFontFamilyType(fontFamily: FontFamilyType): String = fontFamily.name

    @TypeConverter
    fun toFontFamilyType(fontFamily: String): FontFamilyType = try {
        FontFamilyType.valueOf(fontFamily)
    } catch (e: IllegalArgumentException) {
        FontFamilyType.SERIF
    }
}