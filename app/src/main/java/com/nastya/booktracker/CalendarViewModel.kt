package com.nastya.booktracker

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalendarViewModel(val dailyReadingDao: DailyReadingDao, val context: Context) : ViewModel() {
    private val _dailyGoal = MutableLiveData<Int>()
    val dailyGoal: LiveData<Int> = _dailyGoal
    private val _monthlyGoal = MutableLiveData<Int>()
    val monthlyGoal: LiveData<Int> = _monthlyGoal
    private val _yearlyGoal = MutableLiveData<Int>()
    val yearlyGoal: LiveData<Int> = _yearlyGoal

    private val DAY_GOAL_KEY = "daily_goal"
    private val MONTH_GOAL_KEY = "monthly_goal"
    private val YEAR_GOAL_KEY = "yearly_goal"

    init {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val savedDayGoal = sharedPref.getInt(DAY_GOAL_KEY, 1)
        val savedMonthGoal = sharedPref.getInt(MONTH_GOAL_KEY, 1)
        val savedYearGoal = sharedPref.getInt(YEAR_GOAL_KEY, 1)
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
        sharedPref.edit().putInt(DAY_GOAL_KEY, goalPageNew).apply()
    }

    private fun saveMonthGoal(goalPageNew: Int) {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putInt(MONTH_GOAL_KEY, goalPageNew).apply()
    }

    private fun saveYearGoal(goalPageNew: Int) {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putInt(YEAR_GOAL_KEY, goalPageNew).apply()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun dailyProgressGet (date: LocalDate) : Float {
        val progressItem = dailyReadingDao.get(date)
        val currentGoal = _dailyGoal.value ?: 1
        val countPage = progressItem?.countPage ?: 0
        val newProgress = (countPage * 100f).div(currentGoal)
//        Log.d("Cal", "newProgress: " + newProgress)
        return newProgress
    }
}