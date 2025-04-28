package com.nastya.booktracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import coil.transform.Transformation
import kotlinx.coroutines.launch

class AddBookViewModel(val dao: BookDao): ViewModel() {
    var newTaskName = ""
    val books = dao.getAll()
    val booksString = books.map {
        books -> formatBooks(books)
    }

    fun addTask() {
        viewModelScope.launch {
            val book = Book()
            book.bookName = newTaskName
            dao.insert(book)
        }
    }

    fun formatBooks(books: List<Book>): String {
        return books.fold("") {
            str, item -> str + '\n' + formatBook(item)
        }
    }

    fun formatBook(book: Book): String {
        var str = "ID: ${book.bookId}"
        str += '\n' + "Name: ${book.bookName}"
        return str
    }
}