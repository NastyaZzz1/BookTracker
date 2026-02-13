package com.nastya.booktracker.presentation.ui.editBook

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nastya.booktracker.data.local.dao.BookDao
import com.nastya.booktracker.domain.model.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditBookViewModel(bookId: Long, private val bookDao: BookDao) : ViewModel() {
    private val _bookState = MutableStateFlow<Book?>(null)
    val bookState: StateFlow<Book?> = _bookState.asStateFlow()

    val book = bookDao.get(bookId)
    private val _navigateToDetail: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val navigateToDetail: LiveData<Boolean>
        get() = _navigateToDetail

    init {
        viewModelScope.launch {
            bookDao.get(bookId).collect { book ->
                _bookState.value = book
            }
        }
    }

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun onBookNameChanged(bookName: String) {
        _bookState.value?.bookName = bookName
    }

    fun onBookAuthorChanged(bookAuthor: String) {
        _bookState.value?.bookAuthor = bookAuthor
    }

    fun onBookDescChanged(bookDesc: String) {
        _bookState.value?.description = bookDesc
    }

    fun updateTask() {
        viewModelScope.launch {
            bookDao.update(_bookState.value!!)
            _navigateToDetail.value = true
        }
    }

    fun onNavigatedToList() {
        _navigateToDetail.value = false
    }
}