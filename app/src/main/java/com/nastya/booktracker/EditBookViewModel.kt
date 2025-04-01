package com.nastya.booktracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditBookViewModel(bookId: Long) : ViewModel() {

    val book: Book? = BooksViewModel().getBook(bookId)

    private val _navigateToList = MutableLiveData<Boolean>(false)
    val navigateToList: LiveData<Boolean>
        get() = _navigateToList

    fun updateTask() {
        _navigateToList.value = true
    }

    fun deleteTask() {
        _navigateToList.value = true
    }

    fun onNavigatedToList() {
        _navigateToList.value = false
    }

}