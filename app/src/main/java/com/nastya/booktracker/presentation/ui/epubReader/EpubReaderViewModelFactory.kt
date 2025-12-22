package com.nastya.booktracker.presentation.ui.epubReader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nastya.booktracker.data.local.dao.BookDao

class EpubReaderViewModelFactory(
        private val dao: BookDao,
        private val bookId: Long
    )
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(EpubReaderViewModel::class.java)) {
            return EpubReaderViewModel(dao, bookId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}