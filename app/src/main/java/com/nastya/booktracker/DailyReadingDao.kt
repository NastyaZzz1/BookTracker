package com.nastya.booktracker

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate

@Dao
interface DailyReadingDao {
    @Insert
    suspend fun insert(dailyReading: DailyReading)

    @Update
    suspend fun update(dailyReading: DailyReading)

    @Query("SELECT * FROM daily_reading_table WHERE read_date = :readDate")
    suspend fun get(readDate: LocalDate) : DailyReading?

    @Query("SELECT * FROM daily_reading_table ORDER BY dataId DESC")
    suspend fun getAll() : List<DailyReading>

    @Query("SELECT SUM(count_page) FROM daily_reading_table WHERE strftime('%Y', read_date) = :year AND strftime('%m', read_date) = :month")
    suspend fun getTotalPagesReadInMonth(year: String, month: String): Int?
}