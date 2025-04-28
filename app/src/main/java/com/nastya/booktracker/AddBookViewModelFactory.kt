package com.nastya.booktracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AddBookViewModelFactory(private val dao: BookDao)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddBookViewModel::class.java)) {
            return AddBookViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}