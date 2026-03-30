package com.nastya.booktracker.presentation.ui.stat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nastya.booktracker.data.local.dao.DailyReadingDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatViewModel(private val dailyReadingDao: DailyReadingDao) : ViewModel() {
    private val _monthStats = MutableStateFlow<List<MonthStat>>(emptyList())
    val monthStats = _monthStats.asStateFlow()

    fun loadYearStat(year: Int) {
        viewModelScope.launch {
        val stats =  (1..12).map { month ->
            val pages = dailyReadingDao.getTotalPagesReadInMonth(
                year.toString(),
                "%02d".format(month)
            ) ?: 0
            MonthStat(
                month = month,
                pages = pages
            )
        }
            _monthStats.value = stats
        }
    }

    data class MonthStat(
        val month: Int,
        val pages: Long
    )
}