package com.nastya.booktracker.presentation.ui.editBook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nastya.booktracker.data.local.dao.BookDao
import com.nastya.booktracker.data.local.dao.DailyReadingDao

class EditBookViewModelFactory(private val bookId: Long,
                               private val bookDao: BookDao,
                               private val dailyReadingDao: DailyReadingDao
    )
        : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(EditBookViewModel::class.java)) {
            return EditBookViewModel(bookId, bookDao, dailyReadingDao) as T
        }
        throw IllegalArgumentException("Unkown ViewModel")
    }
}