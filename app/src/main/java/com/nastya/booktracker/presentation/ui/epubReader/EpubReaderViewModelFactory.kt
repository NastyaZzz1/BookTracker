package com.nastya.booktracker.presentation.ui.epubReader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nastya.booktracker.data.local.dao.BookDao
import com.nastya.booktracker.data.local.dao.DailyReadingDao
import com.nastya.booktracker.data.local.dao.HighlightDao

class EpubReaderViewModelFactory(
    private val bookDao: BookDao,
    private val dailyReadingDao: DailyReadingDao,
    private val highlightDao: HighlightDao,
    private val bookId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(EpubReaderViewModel::class.java)) {
            return EpubReaderViewModel(bookDao, highlightDao, dailyReadingDao, bookId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}