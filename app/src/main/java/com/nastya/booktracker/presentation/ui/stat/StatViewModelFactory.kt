package com.nastya.booktracker.presentation.ui.stat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nastya.booktracker.data.local.dao.DailyReadingDao
import com.nastya.booktracker.presentation.ui.stat.StatViewModel

class StatViewModelFactory(private val dailyReadingDao: DailyReadingDao)
        : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatViewModel::class.java)) {
            return StatViewModel(dailyReadingDao) as T
        }
        throw IllegalArgumentException("Unkown ViewModel")
    }
}