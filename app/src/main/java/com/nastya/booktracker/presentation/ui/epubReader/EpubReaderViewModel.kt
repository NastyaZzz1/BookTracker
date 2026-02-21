package com.nastya.booktracker.presentation.ui.epubReader

import android.content.Context
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.nastya.booktracker.data.local.dao.BookDao
import com.nastya.booktracker.data.local.dao.DailyReadingDao
import com.nastya.booktracker.data.local.dao.HighlightDao
import com.nastya.booktracker.domain.model.DailyReading
import com.nastya.booktracker.domain.model.Highlight
import com.nastya.booktracker.domain.model.LocatorDto
import com.nastya.booktracker.presentation.ui.EpubRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.readium.r2.navigator.Decoration
import org.readium.r2.navigator.ExperimentalDecorator
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import java.time.LocalDate

class EpubReaderViewModel(
    private val bookDao: BookDao,
    private val highlightDao: HighlightDao,
    private val dailyReadingDao: DailyReadingDao,
    private val bookId: Long
): ViewModel() {
    private val _publication = MutableStateFlow<Publication?>(null)
    val publication: StateFlow<Publication?> = _publication

    fun loadPublication(context: Context, bookPath: String) {
        viewModelScope.launch {
            val epubRepository = EpubRepository(context)
            _publication.value = epubRepository.extractMetadata(bookPath)
        }
    }

    val book = bookDao.getOneFlow(bookId)

    @OptIn(ExperimentalDecorator::class)
    val highlightDecorations: Flow<List<Decoration>> =
        highlightDao.getHighlightsForBookFlow(bookId)
        .map { highlights ->
            highlights.map { highlight ->
                Decoration(
                    id = "highlight_${highlight.id}",
                    locator = highlight.locatorJson
                        .let { Gson().fromJson(it, LocatorDto::class.java) }
                        .toLocator(),
                    style = when (highlight.style) {
                        Highlight.Style.HIGHLIGHT ->
                            Decoration.Style.Highlight(tint = highlight.tint)
                        Highlight.Style.UNDERLINE ->
                            Decoration.Style.Underline(tint = highlight.tint)
                    },
                    extras = mapOf("id" to highlight.id)
                )
            }
        }

    suspend fun addHighlight(
        style: Highlight.Style,
        @ColorInt tint: Int,
        locator: Locator,
        annotation: String = "",
    ) {
        highlightDao.insert(
            Highlight(
                bookId = bookId,
                style = style,
                tint = tint,
                locatorJson = Gson().toJson(
                    LocatorDto.fromLocator(locator)
                ),
                annotation = annotation
            )
        )
    }

    suspend fun updateHighlightStyle(
        highlightId: Long,
        style: Highlight.Style,
        @ColorInt tint: Int
    ) {
        getHighlightById(highlightId)
            ?.copy(
                style = style,
                tint = tint,
            )?.let {
                highlightDao.update(it)
            }
    }

    suspend fun getHighlightById(id: Long): Highlight? {
        return highlightDao.getHighlightById(id)
    }

    fun deleteHighlight(id: Long) {
        viewModelScope.launch {
            highlightDao.deleteHighlightById(id)
        }
    }

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