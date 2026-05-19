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

    private var currentUnits: Units = Units.MINUTES
    private var rawStats: List<MonthStat> = emptyList()

    fun loadYearStat(year: Int) {
        viewModelScope.launch {
        rawStats =  (1..12).map { month ->
            val timeInSeconds = dailyReadingDao.getTotalPagesReadInMonth(
                year = year.toString(),
                month = "%02d".format(month)
            ) ?: 0
            MonthStat(
                month = month,
                seconds = timeInSeconds
            )
        }
        applyUnitConversion()
        }
    }

    fun changeUnits(units: Units) {
        currentUnits = units
        applyUnitConversion()
    }

    fun applyUnitConversion() {
        _monthStats.value = rawStats.map { stat ->
            val convertedValue = when (currentUnits) {
                Units.SECONDS -> stat.seconds
                Units.MINUTES -> stat.seconds / 60
                Units.HOURS -> stat.seconds / 3600
            }
            MonthStat(
                month = stat.month,
                seconds = convertedValue,
                units = currentUnits
            )
        }
    }

    data class MonthStat(
        val month: Int,
        val seconds: Long,
        val units: Units = Units.MINUTES
    )

    enum class Units {
        SECONDS, MINUTES, HOURS
    }
}