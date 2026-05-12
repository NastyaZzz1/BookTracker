package com.nastya.booktracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nastya.booktracker.domain.model.BookSettings

@Dao
interface BookSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookSettings: BookSettings)

    @Query("SELECT * FROM book_settings_table LIMIT 1")
    suspend fun getSettings(): BookSettings?
}