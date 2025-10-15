package com.nastya.booktracker.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_table")
data class Book (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "book_id")
    var bookId: Long = 0L,

    @ColumnInfo(name = "book_name")
    var bookName: String = "",

    @ColumnInfo(name = "book_author")
    var bookAuthor: String = "",

    @ColumnInfo(name = "book_category")
    var category: String = "",

    @ColumnInfo(name = "book_description")
    var description: String = "",

    @ColumnInfo(name = "image_url")
    var imageUrl: String = "",

    @ColumnInfo(name = "all_pages_count")
    var allPagesCount: Int = 0,

    @ColumnInfo(name = "read_pages_count")
    var readPagesCount: Int = 0,

    @ColumnInfo(name = "is_favorite_book")
    var isFavorite: Boolean = false,
)