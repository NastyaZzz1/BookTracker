package com.nastya.booktracker

data class Book (
    var bookId: Long = 0L,
    var bookName: String,
    var bookAuthor: String,
    var category: String,
    var description: String,
    var imageUrl: String,
    var allPagesCount: Int,
    var readPagesCount: Int,
)