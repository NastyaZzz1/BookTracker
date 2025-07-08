package com.nastya.booktracker

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AddBookViewModel(val dao: BookDao): ViewModel() {
    private var newBookName = ""
    private var newBookAuthor = ""
    private var newBookDesc = ""
    private var newImageUrl = ""
    private var newAllPagesCount = 1

    private val _navigateToBack: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val navigateToBack: LiveData<Boolean>
        get() = _navigateToBack

    fun onNewAllPagesCountChanged(bookPage: Int){
        newAllPagesCount = bookPage
    }

    fun onBookNameChanged(bookName: String){
        newBookName = bookName
    }

    fun onBookAuthorChanged(bookAuthor: String){
        newBookAuthor = bookAuthor
    }

    fun onBookDescChanged(bookDesc: String){
        newBookDesc = bookDesc
    }

    fun onBookImgChanged(bookImg: String){
        newImageUrl = bookImg
    }

    fun addTask() {
        viewModelScope.launch {
            val book = Book(
                bookName = newBookName,
                bookAuthor = newBookAuthor,
                description = newBookDesc,
                imageUrl = newImageUrl,
                allPagesCount = newAllPagesCount)
            dao.insert(book)
            _navigateToBack.value = true
        }
    }

    suspend fun isAvailableBook(bookName: String, bookAuthor: String): Boolean {
        return if (dao.countBooksByTitleAndAuthor(bookName, bookAuthor) == 0) true
        else false
    }
}