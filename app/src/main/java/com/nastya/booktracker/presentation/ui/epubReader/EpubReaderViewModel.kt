package com.nastya.booktracker.presentation.ui.epubReader

import android.content.Context
import android.graphics.Color
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
                    locator = getLocator(highlight),
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
        style: Highlight.Style = Highlight.Style.UNDERLINE,
        @ColorInt tint: Int = Color.rgb(124, 198, 247),
        locator: Locator,
        annotation: String = "",
    ): Long {
        return highlightDao.insert(
            Highlight(
                bookId = bookId,
                style = style,
                tint = tint,
                locatorJson = Gson().toJson(LocatorDto.fromLocator(locator)),
                annotation = annotation
            )
        )
    }

    suspend fun updateHighlightStyle(
        highlight: Highlight,
        style: Highlight.Style,
        @ColorInt tint: Int
    ) {
        highlight.copy(style = style, tint = tint)
            .let { highlightDao.update(it) }
    }

    fun saveAnnotation(
        noteText: String,
        highlight: Highlight? = null,
        locator: Locator?
    ) {
        viewModelScope.launch {
            if (highlight != null) {
                highlight.copy(annotation = noteText)
                    .let { highlightDao.update(it) }
            } else {
                locator?.let {
                    addHighlight(annotation = noteText, locator = locator)
                }
            }
        }
    }

    suspend fun getHighlightById(id: Long): Highlight? = highlightDao.getHighlightById(id)

    fun deleteHighlight(id: Long) {
        viewModelScope.launch { highlightDao.deleteHighlightById(id) }
    }

    fun getLocator(highlight: Highlight): Locator {
        return Gson().fromJson(highlight.locatorJson, LocatorDto::class.java).toLocator()
    }

    fun saveProgressToDb(locator: Locator?, presentRead: Int) {
        if(locator == null) return
        viewModelScope.launch {
            bookDao.getNotLive(bookId)?.apply {
                locatorJson = Gson().toJson(LocatorDto.fromLocator(locator))
                progress = presentRead
                bookDao.update(this)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveReadingTime(readingTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val today = LocalDate.now()
            dailyReadingDao.getPagesReadForBookOnDate(today, bookId)?.let {
                dailyReadingDao.update(
                    it.copy (readingTime = it.readingTime + readingTime)
                )
            } ?:
                dailyReadingDao.insert(
                    DailyReading (
                        readDate = today,
                        bookId = bookId,
                        readingTime = readingTime
                    )
                )
        }
    }
}