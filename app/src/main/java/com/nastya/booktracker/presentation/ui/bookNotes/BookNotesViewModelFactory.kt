package com.nastya.booktracker.presentation.ui.bookNotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nastya.booktracker.data.local.dao.HighlightDao

class BookNotesViewModelFactory(
    val highlightDao: HighlightDao,
    private val bookId: Long
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookNotesViewModel::class.java)) {
            return BookNotesViewModel(highlightDao, bookId) as T
        }
        throw IllegalArgumentException("Unkown ViewModel")
    }
}