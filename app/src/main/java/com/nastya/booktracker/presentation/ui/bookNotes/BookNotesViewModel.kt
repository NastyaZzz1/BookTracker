package com.nastya.booktracker.presentation.ui.bookNotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nastya.booktracker.data.local.dao.HighlightDao
import com.nastya.booktracker.domain.model.Highlight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class BookNotesViewModel(
    val highlightDao: HighlightDao,
    bookId: Long
): ViewModel() {
    private val selectedCategory = MutableStateFlow<Highlight.Style?>(null)

    val filteredHighlights: StateFlow<List<Highlight>> = combine(
            highlightDao.getHighlightsForBookFlow(bookId),
            selectedCategory
        ) { allHighlights, category ->

            var result = if (category == null) {
                allHighlights
            } else {
                allHighlights.filter { it.style == category }
            }
            result
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun filterByCategory(category: Highlight.Style?) {
        selectedCategory.value = category
    }
}