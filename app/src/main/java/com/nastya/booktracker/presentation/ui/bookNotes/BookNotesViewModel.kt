package com.nastya.booktracker.presentation.ui.bookNotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nastya.booktracker.data.local.dao.HighlightDao
import com.nastya.booktracker.domain.model.Highlight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookNotesViewModel(
    val highlightDao: HighlightDao
): ViewModel() {
    private val _highlights = MutableStateFlow<List<Highlight>>(emptyList())
    val highlights: StateFlow<List<Highlight>> = _highlights.asStateFlow()

    fun loadNotesForBook(bookId: Long) {
        viewModelScope.launch {
            highlightDao.getHighlightsForBookFlow(bookId)
                .collect { highlights ->
                    _highlights.value = highlights
            }
        }
    }

}