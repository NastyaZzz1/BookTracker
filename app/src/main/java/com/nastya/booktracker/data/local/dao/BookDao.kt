package com.nastya.booktracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nastya.booktracker.domain.model.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert
    suspend fun insert(book: Book)

    @Update
    suspend fun update(book: Book)

    @Delete
    suspend fun delete(book: Book)

    @Query("SELECT * FROM book_table WHERE book_id = :bookId")
    suspend fun getNotLive(bookId: Long): Book?

    @Query("SELECT * FROM book_table WHERE book_id = :bookId")
    fun getBook(bookId: Long): Flow<Book>

    @Query("SELECT * FROM book_table")
    fun getAll(): Flow<List<Book>>

    @Query("SELECT * FROM book_table")
    suspend fun getAllOnce(): List<Book>

    @Query("SELECT COUNT(*) FROM book_table WHERE book_name = :title AND book_author = :author")
    suspend fun countBooksByTitleAndAuthor(title: String, author: String): Int
}