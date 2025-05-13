package com.nastya.booktracker

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_reading_table")
data class DailyReading (
    @PrimaryKey(autoGenerate = true)
    var dataId: Long = 0L,

    @ColumnInfo(name = "read_date")
    var readDate: LocalDate,

    @ColumnInfo(name = "count_page")
    var countPage: Int,
)