package com.nastya.booktracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class EditBookViewModel(bookId: Long, val dao: BookDao) : ViewModel() {
    val book = dao.get(bookId)
    private val _navigateToList = MutableLiveData<Boolean>(false)
    val navigateToList: LiveData<Boolean>
        get() = _navigateToList

    fun onAllPagesCountChanged(bookPage: Int){
        book.value?.allPagesCount = bookPage
    }

    fun onBookNameChanged(bookName: String){
        book.value?.bookName = bookName
    }

    fun onBookAuthorChanged(bookAuthor: String){
        book.value?.bookAuthor = bookAuthor
    }

    fun onBookDescChanged(bookDesc: String){
        book.value?.description = bookDesc
    }

    fun onReadPagesCountChanged(readPages: Int){
        book.value?.readPagesCount = readPages
    }

    fun onBookImgChanged(bookImg: String){
        book.value?.imageUrl = bookImg
    }

    fun updateTask() {
        viewModelScope.launch {
            dao.update(book.value!!)
            _navigateToList.value = true
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            dao.delete(book.value!!)
            _navigateToList.value = true
        }
    }

    fun onNavigatedToList() {
        _navigateToList.value = false
    }

}