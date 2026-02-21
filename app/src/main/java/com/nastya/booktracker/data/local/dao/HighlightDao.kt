package com.nastya.booktracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nastya.booktracker.domain.model.Highlight
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightDao {
    @Insert
    suspend fun insert(highlight: Highlight)

    @Update
    suspend fun update(highlight: Highlight)

    @Query("SELECT * FROM highlight_table WHERE book_id = :bookId")
    fun getHighlightsForBookFlow(bookId: Long): Flow<List<Highlight>>

    @Query("SELECT * FROM highlight_table WHERE id = :id")
    suspend fun getHighlightById(id: Long): Highlight?

    @Query("DELETE FROM highlight_table WHERE id = :id")
    suspend fun deleteHighlightById(id: Long)
}