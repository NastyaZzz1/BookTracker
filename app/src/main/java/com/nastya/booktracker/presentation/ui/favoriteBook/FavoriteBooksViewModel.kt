package com.nastya.booktracker.presentation.ui.favoriteBook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nastya.booktracker.data.local.dao.BookDao
import com.nastya.booktracker.domain.model.Book
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoriteBooksViewModel(val dao: BookDao) : ViewModel() {
    private val _navigateToBook = MutableSharedFlow<Long>()
    val navigateToBook = _navigateToBook.asSharedFlow()

    val favoriteBooks: StateFlow<List<Book>> = dao.getAll()
        .map { books -> books.filter { it.isFavorite } }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    fun onBookClicked(bookId: Long) {
        viewModelScope.launch {
            _navigateToBook.emit(bookId)
        }
    }

    fun toggleBookIsFavorite(bookId: Long) {
        viewModelScope.launch {
            val book = dao.getBook(bookId).first()
            dao.update(book)
        }
    }
}