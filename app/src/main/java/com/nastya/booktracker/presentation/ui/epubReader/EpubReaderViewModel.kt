package com.nastya.booktracker.presentation.ui.epubReader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.nastya.booktracker.data.local.dao.BookDao
import com.nastya.booktracker.domain.model.LocatorDto
import kotlinx.coroutines.launch
import org.readium.r2.shared.publication.Locator

class EpubReaderViewModel(
    private val bookDao: BookDao,
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
}