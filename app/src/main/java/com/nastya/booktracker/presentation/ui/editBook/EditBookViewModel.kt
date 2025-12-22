package com.nastya.booktracker.presentation.ui.editBook

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nastya.booktracker.data.local.dao.BookDao
import kotlinx.coroutines.launch

class EditBookViewModel(bookId: Long, private val bookDao: BookDao) : ViewModel() {
    val book = bookDao.get(bookId)
    private val _navigateToDetail: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val navigateToDetail: LiveData<Boolean>
        get() = _navigateToDetail

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun onBookNameChanged(bookName: String) {
        book.value?.bookName = bookName
    }

    fun onBookAuthorChanged(bookAuthor: String) {
        book.value?.bookAuthor = bookAuthor
    }

    fun onBookDescChanged(bookDesc: String) {
        book.value?.description = bookDesc
    }

    fun updateTask() {
        viewModelScope.launch {
            bookDao.update(book.value!!)
            _navigateToDetail.value = true
        }
    }

    fun onNavigatedToList() {
        _navigateToDetail.value = false
    }
}