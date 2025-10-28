package com.nastya.booktracker.presentation.ui.bookDetail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nastya.booktracker.data.local.dao.BookDao
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class BookDetailViewModel(bookId: Long, private val bookDao: BookDao): ViewModel() {
    val book = bookDao.get(bookId)
    private val _navigateToList = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navigateToList: SharedFlow<Unit> = _navigateToList

    fun isFavoriteChanged() {
        book.value?.let { currentBook ->
            currentBook.isFavorite = !currentBook.isFavorite

            viewModelScope.launch {
                bookDao.update(book.value!!)
            }
        }
    }

    fun showDeleteConfirmationDialog(context: Context) {
        val alertDialog = MaterialAlertDialogBuilder(context)
            .setTitle("Удаление книги")
            .setMessage("Вы точно хотите удалить книгу?")
            .setPositiveButton("Да") { _, _ ->
                deleteTaskAndNavigate()
            }
            .setNegativeButton("Отмена", null)
            .create()

        alertDialog.show()
    }

    private fun deleteTaskAndNavigate() {
        viewModelScope.launch {
            bookDao.delete(book.value!!)
            _navigateToList.emit(Unit)
        }
    }
}