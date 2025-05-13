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
    suspend fun insert(daily_reading: DailyReading)

    @Update
    suspend fun update(daily_reading: DailyReading)

    @Query("SELECT * FROM daily_reading_table WHERE read_date = :readDate")
    suspend fun get(readDate: LocalDate) : DailyReading?

    @Query("SELECT * FROM daily_reading_table ORDER BY dataId DESC")
    fun getAll() : LiveData<List<DailyReading>>
}