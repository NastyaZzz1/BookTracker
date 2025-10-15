package com.nastya.booktracker.presentation.ui.stat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarEntry
import com.nastya.booktracker.data.local.dao.DailyReadingDao
import kotlinx.coroutines.launch
import java.time.LocalDate

class StatViewModel(private val dailyReadingDao: DailyReadingDao) : ViewModel() {
    private val _barChartData = MutableLiveData<List<BarEntry>>()
    val barChartData: LiveData<List<BarEntry>> = _barChartData

    @RequiresApi(Build.VERSION_CODES.O)
    fun getBarChartData() {
        viewModelScope.launch {
            val entries = buildList {
                repeat(11) { index ->
                    val countPage = dailyReadingDao.getTotalPagesReadInMonth(
                        LocalDate.now().year.toString(),
                        "%02d".format(index+1)
                    ) ?: 0
                    add(BarEntry(index + 1.toFloat(), countPage.toFloat()))
                }
            }
            _barChartData.value = entries
        }
    }
}