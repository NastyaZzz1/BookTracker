package com.nastya.booktracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BookViewModelFactory(private val dao: BookDao)
                : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(BooksViewModel::class.java)) {
            return BooksViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}