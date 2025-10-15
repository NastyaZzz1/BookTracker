package com.nastya.booktracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nastya.booktracker.domain.model.DailyReading
import com.nastya.booktracker.domain.model.DailyReadingWithTitle
import java.time.LocalDate

@Dao
interface DailyReadingDao {
    @Insert
    suspend fun insert(dailyReading: DailyReading)

    @Update
    suspend fun update(dailyReading: DailyReading)

    @Query("""
       SELECT dr.dataId, dr.read_date as readDate, dr.count_page as countPage, b.book_name as bookTitle
       FROM daily_reading_table dr 
       JOIN book_table b ON dr.book_id = b.book_id
       WHERE dr.read_date = :readDate
    """)
    suspend fun getAllBookOnDate(readDate: LocalDate) : List<DailyReadingWithTitle>

    @Query("""
       SELECT dr.dataId, dr.read_date as readDate, dr.count_page as countPage, b.book_name as bookTitle
       FROM daily_reading_table dr 
       JOIN book_table b ON dr.book_id = b.book_id
       WHERE dr.read_date = :readDate and dr.book_id = :bookId
    """)
    suspend fun getPagesReadForBookOnDate(readDate: LocalDate, bookId: Long?) : DailyReadingWithTitle?

    @Query("SELECT SUM(count_page) FROM daily_reading_table WHERE read_date = :readDate")
    suspend fun getAllPagesOfBook(readDate: LocalDate) : Int?

    @Query("SELECT SUM(count_page) FROM daily_reading_table WHERE strftime('%Y', read_date) = :year AND strftime('%m', read_date) = :month")
    suspend fun getTotalPagesReadInMonth(year: String, month: String): Int?
}