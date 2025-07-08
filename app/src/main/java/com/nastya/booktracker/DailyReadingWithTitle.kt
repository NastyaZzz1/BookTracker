package com.nastya.booktracker

import java.time.LocalDate

data class DailyReadingWithTitle (
    val dataId: Long,
    val readDate: LocalDate,
    val countPage: Int,
    val bookTitle: String
)