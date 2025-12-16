package com.nastya.booktracker.presentation.ui.addBook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nastya.booktracker.data.local.dao.BookDao
import com.nastya.booktracker.domain.model.Book
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class AddBookViewModel(val dao: BookDao): ViewModel() {
    private var newBookName = ""
    private var newBookAuthor = ""
    private var newBookDesc = ""
    private var newImageUrl = ""
    private var newAllPagesCount = 1

    private val _navigateToBack = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navigateToBack: SharedFlow<Unit> = _navigateToBack

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

//    fun addTask() {
//        viewModelScope.launch {
//            val book = Book(
//                bookName = newBookName,
//                bookAuthor = newBookAuthor,
//                description = newBookDesc,
//                imageData = newImageUrl,
//                allPagesCount = newAllPagesCount
//            )
//            dao.insert(book)
//            _navigateToBack.emit(Unit)
//        }
//    }

    suspend fun isAvailableBook(bookName: String, bookAuthor: String): Boolean {
        return if (dao.countBooksByTitleAndAuthor(bookName, bookAuthor) == 0) true
        else false
    }
}