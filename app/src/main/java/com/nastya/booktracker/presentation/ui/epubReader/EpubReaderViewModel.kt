package com.nastya.booktracker.presentation.ui.epubReader

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.nastya.booktracker.data.local.dao.BookDao
import com.nastya.booktracker.data.local.dao.DailyReadingDao
import com.nastya.booktracker.domain.model.DailyReading
import com.nastya.booktracker.domain.model.LocatorDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Locator
import java.time.LocalDate

class EpubReaderViewModel(
    private val bookDao: BookDao,
    private val dailyReadingDao: DailyReadingDao,
    private val bookId: Long
): ViewModel() {
    val book = bookDao.getOneFlow(bookId)

    fun saveProgressToDb(
        locator: Locator?,
        presentRead: Int
    ) {
        if(locator == null) return

        viewModelScope.launch {
            val bookEntity = bookDao.getNotLive(bookId) ?: return@launch

            val dto = LocatorDto.fromLocator(locator)
            bookEntity.locatorJson = Gson().toJson(dto)
            bookEntity.progress = presentRead

            bookDao.update(bookEntity)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveReadingTime(readingTime: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val readingTimeItem = dailyReadingDao.getPagesReadForBookOnDate(LocalDate.now(), bookId)

                if(readingTimeItem != null) {
                    val updatedReading = readingTimeItem.copy (
                        readingTime = readingTimeItem.readingTime + readingTime
                    )
                    dailyReadingDao.update(updatedReading)
                }
                else {
                    val dailyReadingEntity = DailyReading (
                        readDate = LocalDate.now(),
                        bookId = bookId,
                        readingTime = readingTime
                    )
                    dailyReadingDao.insert(dailyReadingEntity)
                }
            }
        }
    }
}