package com.nastya.booktracker.presentation.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.nastya.booktracker.data.local.dao.DailyReadingDao
import java.time.LocalDate
import androidx.lifecycle.viewModelScope
import com.nastya.booktracker.GoalPreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.String

class CalendarViewModel(
    private val dailyReadingDao: DailyReadingDao,
    private val goalRepo: GoalPreferencesRepository
): ViewModel() {
    val dailyGoal = goalRepo.dailyGoal
    val monthlyGoal = goalRepo.monthlyGoal
    val yearlyGoal = goalRepo.yearlyGoal

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    fun onDayGoalChanged(value: Int) =
        goalRepo.setDailyGoal(value)

    fun onMonthGoalChanged(value: Int) =
        goalRepo.setMonthlyGoal(value)

    fun onYearGoalChanged(value: Int) =
        goalRepo.setYearlyGoal(value)

    suspend fun getDailyProgress (date: LocalDate) : Float {
        val goal = dailyGoal.value
        val time = dailyReadingDao.getAllTimeOfBook(date) ?: 0
        return (time * 100f / goal)
    }

    suspend fun getBooksForDate(date: LocalDate) =
        dailyReadingDao.getAllBookOnDate(date)

    suspend fun getReadingTimeForDate(date: LocalDate) =
        dailyReadingDao.getAllTimeOfBook(date) ?: 0

    fun formatTimeMinutes(readingTime: Long) = String.format("%02d:%02d", readingTime / 60, readingTime % 60)

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatLocalDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
        return date.format(formatter)
    }

    fun onDayClicked(date: LocalDate) {
        viewModelScope.launch {
            _events.emit(UiEvent.ShowDayDetailDialog(date))
        }
    }

    fun onChangeGoalClicked(type: GoalType) {
        viewModelScope.launch {
            _events.emit(UiEvent.ShowGoalDialog(type))
        }
    }
}

sealed class UiEvent {
    data class ShowGoalDialog(val goalType: GoalType) : UiEvent()
    data class ShowDayDetailDialog(val date: LocalDate) : UiEvent()
}

enum class GoalType {
    DAY, MONTH, YEAR
}