package com.nastya.booktracker.presentation.ui.bookList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nastya.booktracker.data.local.dao.BookDao

class BookViewModelFactory(private val dao: BookDao)
                : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(BooksViewModel::class.java)) {
            return BooksViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}