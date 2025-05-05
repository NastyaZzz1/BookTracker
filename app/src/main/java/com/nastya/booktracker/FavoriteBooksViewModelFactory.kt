package com.nastya.booktracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FavoriteBooksViewModelFactory(private val dao: BookDao)
                :ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(FavoriteBooksViewModel::class.java)) {
            return FavoriteBooksViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}