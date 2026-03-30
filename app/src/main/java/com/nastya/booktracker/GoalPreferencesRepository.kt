package com.nastya.booktracker

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GoalPreferencesRepository(
    context: Context
) {
    private val prefs =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val dayGoalKey = "daily_goal"
    private val monthGoalKey = "monthly_goal"
    private val yearGoalKey = "yearly_goal"

    private val _dailyGoal = MutableStateFlow(
        prefs.getInt(dayGoalKey, 1)
    )
    val dailyGoal = _dailyGoal.asStateFlow()

    private val _monthlyGoal = MutableStateFlow(
        prefs.getInt(monthGoalKey, 1)
    )
    val monthlyGoal = _monthlyGoal.asStateFlow()

    private val _yearlyGoal = MutableStateFlow(
        prefs.getInt(yearGoalKey, 1)
    )
    val yearlyGoal = _yearlyGoal.asStateFlow()

    fun setDailyGoal(value: Int) {
        prefs.edit { putInt(dayGoalKey, value) }
        _dailyGoal.value = value
    }

    fun setMonthlyGoal(value: Int) {
        prefs.edit { putInt(monthGoalKey, value) }
        _monthlyGoal.value = value
    }

    fun setYearlyGoal(value: Int) {
        prefs.edit { putInt(yearGoalKey, value) }
        _yearlyGoal.value = value
    }
}