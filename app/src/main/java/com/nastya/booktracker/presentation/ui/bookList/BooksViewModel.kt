package com.nastya.booktracker.presentation.ui.bookList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nastya.booktracker.data.local.dao.BookDao
import com.nastya.booktracker.domain.model.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BooksViewModel(val dao: BookDao) : ViewModel() {
    private val _navigateToDetail = MutableStateFlow<Long?>(null)
    val navigateToDetail: StateFlow<Long?> = _navigateToDetail

    sealed class SortedState() {
        object None : SortedState()
        object Desc : SortedState()
        object Asc : SortedState()
    }

    private val _sortedBooksState = MutableStateFlow<SortedState>(SortedState.None)
    val sortedBooksState = _sortedBooksState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)

    val filteredBooks: StateFlow<List<Book>> = combine(
        dao.getAllFlow(),
        _sortedBooksState,
        _selectedCategory
    ) { allBooks, sortState, category ->

        var result = if (category == null || category == "all") {
            allBooks
        } else {
            allBooks.filter { it.category == category}
        }

        result = when(sortState) {
            SortedState.None -> result
            SortedState.Asc -> result.sortedByDescending { it.bookName }
            SortedState.Desc -> result.sortedBy { it.bookName }
        }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun filterByCategory(category: String) {
        _selectedCategory.value = if (category == "all") null else category
    }

    fun changeSortedState() {
        _sortedBooksState.value = when (_sortedBooksState.value) {
            SortedState.None -> SortedState.Desc
            SortedState.Desc -> SortedState.Asc
            SortedState.Asc -> SortedState.None
        }
    }

    fun updateAllCategories() {
        viewModelScope.launch {
            val booksList = dao.getAllOnce()

            val updatedBooks = booksList.map { book ->
                val progress = book.progress

                val newCategory = when (progress) {
                    0 -> "want"
                    100 -> "past"
                    else -> "reading"
                }

                if (book.category != newCategory) book.copy(category = newCategory)
                else book
            }

            updatedBooks.forEach { updatedBook ->
                dao.update(updatedBook)
            }
        }
    }

    fun onBookClicked(bookId: Long) {
        viewModelScope.launch {
            _navigateToDetail.value = bookId
        }
    }

    fun onBookNavigated() {
        viewModelScope.launch {
            _navigateToDetail.value = null
        }
    }

    fun toggleBookIsFavorite(bookId: Long) {
        viewModelScope.launch {
            val book = dao.getNotLive(bookId)
            book!!.isFavorite = !book.isFavorite
            dao.update(book)
        }
    }
}