package com.nastya.booktracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EditBookViewModelFactory(private val bookId: Long)
        : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(EditBookViewModel::class.java)) {
            return EditBookViewModel(bookId) as T
        }
        throw IllegalArgumentException("Unkown ViewModel")
    }
}