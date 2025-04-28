package com.nastya.booktracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EditBookViewModelFactory(private val bookId: Long,
                                private val dao: BookDao)
        : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(EditBookViewModel::class.java)) {
            return EditBookViewModel(bookId, dao) as T
        }
        throw IllegalArgumentException("Unkown ViewModel")
    }
}