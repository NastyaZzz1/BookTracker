package com.nastya.booktracker.presentation.ui.calendar

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.widget.EditText
import com.nastya.booktracker.R
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nastya.booktracker.data.local.dao.DailyReadingDao
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener
import kotlin.String

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
        sharedPref.edit { putInt(dayGoalKey, goalPageNew) }
    }

    private fun saveMonthGoal(goalPageNew: Int) {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref.edit { putInt(monthGoalKey, goalPageNew) }
    }

    private fun saveYearGoal(goalPageNew: Int) {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref.edit { putInt(yearGoalKey, goalPageNew) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun dailyProgressGet (date: LocalDate) : Float {
        val currentGoal = _dailyGoal.value ?: 1
        val countPage = dailyReadingDao.getAllTimeOfBook(date) ?: 0
        val newProgress = (countPage * 100f).div(currentGoal)
        return newProgress
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun showInfDialogDetailData(context: Context, date: LocalDate) {
        val booksProgress = dailyReadingDao.getAllBookOnDate(date)
        val readingTime = dailyReadingDao.getAllTimeOfBook(date) ?: 0

        val message = booksProgress.joinToString(separator = "\n") { reading ->
            "${reading.bookTitle}: ${timeFormated(reading.readingTime)}".trimIndent()
        }
        val dateFormat = formatLocalDate(date)
        val alertDialog = MaterialAlertDialogBuilder(context)
            .setTitle(dateFormat)
            .setMessage("Время чтения: ${timeFormated(readingTime)}\n$message")
            .setNegativeButton("Окей", null)
            .create()
        alertDialog.show()
    }

    fun timeFormated(readingTime: Long) = String.format("%02d:%02d", readingTime / 60, readingTime % 60)

    @RequiresApi(Build.VERSION_CODES.O)
    fun showInfDialogChangeGoal(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_goal, null)
        val alertDialog = MaterialAlertDialogBuilder(context)
            .setTitle("Изменить цели")
            .setView(dialogView)
            .setPositiveButton("Окей", null)
            .create()

        val dayGoal = dialogView.findViewById<EditText>(R.id.day_goal)
        val monthGoal = dialogView.findViewById<EditText>(R.id.month_goal)
        val yearGoal = dialogView.findViewById<EditText>(R.id.year_goal)

        dayGoal.setText(_dailyGoal.value.toString())
        monthGoal.setText(_monthlyGoal.value.toString())
        yearGoal.setText(_yearlyGoal.value.toString())

        dayGoal.addTextChangedListener { str ->
            str.toString().toIntOrNull()?.let { goalPage ->
                onDayGoalChanged(goalPage)
            }
        }

        monthGoal.addTextChangedListener { str ->
            str.toString().toIntOrNull()?.let { goalPage ->
                onMonthGoalChanged(goalPage)
            }
        }

        yearGoal.addTextChangedListener { str ->
            str.toString().toIntOrNull()?.let { goalPage ->
                onYearGoalChanged(goalPage)
            }
        }

        alertDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatLocalDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
        return date.format(formatter)
    }
}