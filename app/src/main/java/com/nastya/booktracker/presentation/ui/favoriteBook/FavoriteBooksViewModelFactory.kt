package com.nastya.booktracker.presentation.ui.favoriteBook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nastya.booktracker.data.local.dao.BookDao

class FavoriteBooksViewModelFactory(private val dao: BookDao)
                : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(FavoriteBooksViewModel::class.java)) {
            return FavoriteBooksViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}