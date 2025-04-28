package com.nastya.booktracker

import android.graphics.Color

data class DateProgress (
    val year: Int,
    val month: Int,
    val day: Int,
    val progress: Float,
    val startColor: Int = Color.RED,
    val endColor: Int = Color.BLUE
)