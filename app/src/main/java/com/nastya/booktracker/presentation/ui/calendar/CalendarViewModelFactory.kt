package com.nastya.booktracker.presentation.ui.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nastya.booktracker.data.local.dao.DailyReadingDao

class CalendarViewModelFactory(val dailyReadingDao: DailyReadingDao,
                               val context: Context
)
        : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            return CalendarViewModel(dailyReadingDao, context) as T
        }
        throw IllegalArgumentException("Unkown ViewModel")
    }
}