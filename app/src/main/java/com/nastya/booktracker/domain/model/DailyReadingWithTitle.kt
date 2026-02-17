package com.nastya.booktracker.domain.model

import java.time.LocalDate

data class DailyReadingWithTitle (
    val dataId: Long,
    val readDate: LocalDate,
    val readingTime: Long,
    val bookTitle: String
)