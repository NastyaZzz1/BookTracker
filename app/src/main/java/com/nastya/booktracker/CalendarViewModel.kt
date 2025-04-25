package com.nastya.booktracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CalendarViewModel : ViewModel() {

    val initialDateProgress = listOf(
        DateProgress(2025, 4, 7, 0.7f),
        DateProgress(2025, 4, 2, 0.5f),
        DateProgress(2025, 4, 4, 0.3f),
    )

    private val _progressData: MutableLiveData<List<DateProgress>> = MutableLiveData(initialDateProgress)
    val progressData: LiveData<List<DateProgress>> = _progressData


}