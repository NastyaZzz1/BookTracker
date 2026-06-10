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
        _dailyGoal.value = value
        _monthlyGoal.value = value * 30
        _yearlyGoal.value = value * 30 * 12
        prefs.edit { putInt(dayGoalKey, value) }
        prefs.edit { putInt(monthGoalKey, _monthlyGoal.value) }
        prefs.edit { putInt(yearGoalKey, _yearlyGoal.value) }
    }

    fun setMonthlyGoal(value: Int) {
        _dailyGoal.value = value / 30
        _monthlyGoal.value = value
        _yearlyGoal.value = value * 12
        prefs.edit { putInt(dayGoalKey, _dailyGoal.value) }
        prefs.edit { putInt(monthGoalKey, value) }
        prefs.edit { putInt(yearGoalKey, _yearlyGoal.value) }
    }

    fun setYearlyGoal(value: Int) {
        _dailyGoal.value = value / 12 / 30
        _monthlyGoal.value = value / 12
        _yearlyGoal.value = value
        prefs.edit { putInt(dayGoalKey, _dailyGoal.value) }
        prefs.edit { putInt(monthGoalKey, _monthlyGoal.value) }
        prefs.edit { putInt(yearGoalKey, value) }
    }
}