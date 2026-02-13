package com.nastya.booktracker.presentation.ui.bookDetail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nastya.booktracker.data.local.dao.BookDao
import com.nastya.booktracker.domain.model.Book
import com.nastya.booktracker.presentation.ui.BookFileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookDetailViewModel(bookId: Long, private val bookDao: BookDao): ViewModel() {
    private val _bookState = MutableStateFlow<Book?>(null)
    val bookState: StateFlow<Book?> = _bookState.asStateFlow()

    private val _navigateToList = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navigateToList: SharedFlow<Unit> = _navigateToList

    init {
        viewModelScope.launch {
            bookDao.get(bookId).collect { book ->
                _bookState.value = book
            }
        }
    }

    fun isFavoriteChanged() {
        viewModelScope.launch {
            _bookState.value?.let { currentBook ->
                bookDao.update(
                    currentBook.copy(isFavorite = !currentBook.isFavorite)
                )
            }
        }
    }

    fun showDeleteConfirmationDialog(context: Context) {
        val alertDialog = MaterialAlertDialogBuilder(context)
            .setTitle("Удаление книги")
            .setMessage("Вы точно хотите удалить книгу?")
            .setPositiveButton("Да") { _, _ ->
                deleteTaskAndNavigate(context)
            }
            .setNegativeButton("Отмена", null)
            .create()

        alertDialog.show()
    }


    private fun deleteTaskAndNavigate(context: Context) {
        viewModelScope.launch {
            BookFileManager.deleteBook(context, _bookState.value!!.fileNameFromUri)
            bookDao.delete(_bookState.value!!)
            _navigateToList.emit(Unit)
        }
    }
}