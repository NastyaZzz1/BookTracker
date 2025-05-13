package com.nastya.booktracker

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate

class CalendarViewModel(private val dailyReadingDao: DailyReadingDao, val context: Context) : ViewModel() {
    private val _dailyGoal = MutableLiveData<Int>()
    val dailyGoal: LiveData<Int> = _dailyGoal
    private val _monthlyGoal = MutableLiveData<Int>()
    val monthlyGoal: LiveData<Int> = _monthlyGoal
    private val _yearlyGoal = MutableLiveData<Int>()
    val yearlyGoal: LiveData<Int> = _yearlyGoal

    private val dayGoalKey = "daily_goal"
    private val monthGoalKey = "monthly_goal"
    private val yearGoalKey = "yearly_goal"

    init {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val savedDayGoal = sharedPref.getInt(dayGoalKey, 1)
        val savedMonthGoal = sharedPref.getInt(monthGoalKey, 1)
        val savedYearGoal = sharedPref.getInt(yearGoalKey, 1)
        _dailyGoal.value = savedDayGoal
        _monthlyGoal.value = savedMonthGoal
        _yearlyGoal.value = savedYearGoal
    }

    fun onDayGoalChanged(goalPageNew: Int) {
        _dailyGoal.value = goalPageNew
        saveDayGoal(goalPageNew)
    }

    fun onMonthGoalChanged(goalPageNew: Int) {
        _monthlyGoal.value = goalPageNew
        saveMonthGoal(goalPageNew)
    }

    fun onYearGoalChanged(goalPageNew: Int) {
        _yearlyGoal.value = goalPageNew
        saveYearGoal(goalPageNew)
    }

    private fun saveDayGoal(goalPageNew: Int) {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putInt(dayGoalKey, goalPageNew).apply()
    }

    private fun saveMonthGoal(goalPageNew: Int) {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putInt(monthGoalKey, goalPageNew).apply()
    }

    private fun saveYearGoal(goalPageNew: Int) {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putInt(yearGoalKey, goalPageNew).apply()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun dailyProgressGet (date: LocalDate) : Float {
        val progressItem = dailyReadingDao.get(date)
        val currentGoal = _dailyGoal.value ?: 1
        val countPage = progressItem?.countPage ?: 0
        val newProgress = (countPage * 100f).div(currentGoal)
        return newProgress
    }
}