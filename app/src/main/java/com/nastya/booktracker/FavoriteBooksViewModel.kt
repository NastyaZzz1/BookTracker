package com.nastya.booktracker

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class FavoriteBooksViewModel(val dao: BookDao) : ViewModel() {
    private val _navigateToBook = MutableLiveData<Long?>()
    val navigateToBook: LiveData<Long?>
        get() = _navigateToBook

    fun onBookClicked(bookId: Long) {
        _navigateToBook.value = bookId
    }

    fun onBookNavigated() {
        _navigateToBook.value = null
    }

    private val _books = dao.getAll()
    val books: LiveData<List<Book>> = _books

    private var _favoriteProducts = MutableLiveData<List<Book>>(emptyList())
    var favoriteProducts: LiveData<List<Book>> = _favoriteProducts

    fun toggleBookIsFavorite(bookId: Long) {
        viewModelScope.launch {
            val book = dao.getNotLive(bookId)
            if(book != null) {
                dao.update(book)
            } else {
                Log.e("BooksViewModel", "Книга с bookId=$bookId не найдена.")
            }
        }
    }

    fun filterByFavorite() {
        books.observeForever { currentList ->
            _favoriteProducts.value = currentList?.filter { it.isFavorite } ?: emptyList()
        }
    }
}