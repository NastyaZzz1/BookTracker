package com.nastya.booktracker.presentation.ui.addBookEpub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nastya.booktracker.data.local.dao.BookDao

class AddBookEpubViewModelFactory(private val dao: BookDao)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddBookEpubViewModel::class.java)) {
            return AddBookEpubViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}