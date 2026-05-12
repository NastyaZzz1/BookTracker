package com.nastya.booktracker.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nastya.booktracker.presentation.ui.FontFamilyType
import com.nastya.booktracker.presentation.ui.ThemeType
import kotlin.Double

@Entity(tableName = "book_settings_table")
data class BookSettings (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0L,

    @ColumnInfo(name = "font_size")
    var fontSize: Double = 1.0,

    @ColumnInfo(name = "font_family")
    var fontFamily: FontFamilyType = FontFamilyType.SERIF,

    @ColumnInfo(name = "theme")
    var theme: ThemeType = ThemeType.LIGHT,

    @ColumnInfo(name = "font_index")
    var fontIndex: Int = 1
)