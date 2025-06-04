package com.nastya.booktracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StatViewModelFactory(private val dailyReadingDao: DailyReadingDao)
        : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatViewModel::class.java)) {
            return StatViewModel(dailyReadingDao) as T
        }
        throw IllegalArgumentException("Unkown ViewModel")
    }
}