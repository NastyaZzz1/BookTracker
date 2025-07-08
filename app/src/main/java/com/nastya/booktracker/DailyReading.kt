package com.nastya.booktracker

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_reading_table",
        foreignKeys = [ForeignKey(
            entity = Book::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )])
data class DailyReading (
    @PrimaryKey(autoGenerate = true)
    var dataId: Long = 0L,

    @ColumnInfo(name = "read_date")
    var readDate: LocalDate,

    @ColumnInfo(name = "book_id")
    var bookId: Long = 0L,

    @ColumnInfo(name = "count_page")
    var countPage: Int,
)