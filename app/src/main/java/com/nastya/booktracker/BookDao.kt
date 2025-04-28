package com.nastya.booktracker

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BookDao {
    @Insert
    suspend fun insert(book: Book)

    @Update
    suspend fun update(book: Book)

    @Delete
    suspend fun delete(book: Book)

    @Query("SELECT * FROM book_table WHERE bookId = :bookId")
    fun get(bookId: Long) : LiveData<Book>

    @Query("SELECT * FROM book_table ORDER BY bookId DESC")
    fun getAll() : LiveData<List<Book>>
}