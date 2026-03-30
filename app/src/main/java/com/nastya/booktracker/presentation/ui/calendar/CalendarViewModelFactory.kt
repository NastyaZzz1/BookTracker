package com.nastya.booktracker.presentation.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nastya.booktracker.GoalPreferencesRepository
import com.nastya.booktracker.data.local.dao.DailyReadingDao

class CalendarViewModelFactory(
    val dailyReadingDao: DailyReadingDao,
    val goalRepo: GoalPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            return CalendarViewModel(dailyReadingDao, goalRepo) as T
        }
        throw IllegalArgumentException("Unkown ViewModel")
    }
}