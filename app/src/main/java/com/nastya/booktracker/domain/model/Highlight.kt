package com.nastya.booktracker.domain.model

import androidx.annotation.ColorInt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "highlight_table")
data class Highlight(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0,

    @ColumnInfo(name = "book_id")
    val bookId: Long,

    @ColumnInfo(name = "style")
    var style: Style,

    @ColumnInfo(name = "tint", defaultValue = "0")
    @ColorInt
    var tint: Int,

    @ColumnInfo(name = "locations")
    var locatorJson: String,

    @ColumnInfo(name = "annotation", defaultValue = "")
    var annotation: String = "",
) {
    enum class Style(val value: String) {
        HIGHLIGHT("highlight"),
        UNDERLINE("underline"),
    }
}
