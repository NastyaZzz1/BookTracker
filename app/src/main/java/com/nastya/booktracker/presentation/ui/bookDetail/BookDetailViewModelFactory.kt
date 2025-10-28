package com.nastya.booktracker.presentation.ui.bookDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nastya.booktracker.data.local.dao.BookDao

class BookDetailViewModelFactory(val bookId: Long, private val dao: BookDao)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(BookDetailViewModel::class.java)) {
            return BookDetailViewModel(bookId,dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}