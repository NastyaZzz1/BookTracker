package com.nastya.booktracker

data class Book (
    var bookId: Long = 0L,
    var bookName: String = "",
    var bookAuthor: String = "",
    var description: String = "",
)