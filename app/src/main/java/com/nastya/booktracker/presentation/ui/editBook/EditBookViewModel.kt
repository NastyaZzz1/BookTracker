package com.nastya.booktracker.presentation.ui.editBook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nastya.booktracker.data.local.dao.BookDao
import com.nastya.booktracker.domain.model.Book
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditBookViewModel(
    bookId: Long,
    private val bookDao: BookDao
): ViewModel() {
    private val _bookState = MutableStateFlow<Book?>(null)
    val bookState: StateFlow<Book?> = _bookState.asStateFlow()

    private val _fieldErrors = MutableStateFlow<FieldErrors?>(null)
    val fieldErrors = _fieldErrors.asStateFlow()

    private val _saveSuccess = MutableSharedFlow<Unit>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    private val _navigateToDetail = MutableSharedFlow<Unit>()
    val navigateToDetail = _navigateToDetail.asSharedFlow()

    init {
        viewModelScope.launch {
            bookDao.getBook(bookId)
                .collect { book ->
                    _bookState.value = book
                }
        }
    }

    fun onBookNameChanged(bookName: String) {
        _bookState.update { it?.copy(bookName = bookName) }
        _fieldErrors.update { it?.copy(nameError = null) }
    }

    fun onBookAuthorChanged(bookAuthor: String) {
        _bookState.update { it?.copy(bookAuthor = bookAuthor) }
        _fieldErrors.update { it?.copy(authorError = null) }
    }

    fun onBookDescChanged(bookDesc: String) {
        _bookState.update { it?.copy(description = bookDesc) }
    }

    fun updateTask() {
        viewModelScope.launch {
            val book = _bookState.value ?: return@launch

            var nameError: String? = null
            var authorError: String? = null

            if (book.bookName.isBlank()) nameError = "Введите название"
            if (book.bookAuthor.isBlank()) authorError = "Введите автора"

            if (nameError != null || authorError != null) {
                _fieldErrors.value = FieldErrors(nameError, authorError)
                return@launch
            }

            bookDao.update(book)
            _fieldErrors.value = null
            _saveSuccess.emit(Unit)
            _navigateToDetail.emit(Unit)
        }
    }

    data class FieldErrors(
        val nameError: String? = null,
        val authorError: String? = null
    )
}