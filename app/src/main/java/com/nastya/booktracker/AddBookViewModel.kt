package com.nastya.booktracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AddBookViewModel(val dao: BookDao): ViewModel() {
    private var newBookName = ""
    private var newBookAuthor = ""
    private var newBookDesc = ""
    private var newImageUrl = ""
    private var newAllPagesCount = 1

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
        }
    }
}