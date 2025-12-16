package com.nastya.booktracker.presentation.ui.editBook

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nastya.booktracker.data.local.dao.BookDao
import com.nastya.booktracker.data.local.dao.DailyReadingDao
import com.nastya.booktracker.domain.model.DailyReading
import kotlinx.coroutines.launch
import java.time.LocalDate

class EditBookViewModel(bookId: Long, private val bookDao: BookDao, private val dailyReadingDao: DailyReadingDao) : ViewModel() {
    val book = bookDao.get(bookId)
    private val _navigateToDetail: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val navigateToDetail: LiveData<Boolean>
        get() = _navigateToDetail

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun onAllPagesCountChanged(bookPage: Int) {
        book.value?.allPagesCount = bookPage
    }

    fun onBookNameChanged(bookName: String) {
        book.value?.bookName = bookName
    }

    fun onBookAuthorChanged(bookAuthor: String) {
        book.value?.bookAuthor = bookAuthor
    }

    fun onBookDescChanged(bookDesc: String) {
        book.value?.description = bookDesc
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onReadPagesCountChanged(newReadPages: Int) {
        if (book.value?.readPagesCount?.let { it <= newReadPages } == true) {
            if(book.value?.allPagesCount?.let {it >= newReadPages} == true) {
                dailyReadingInsert(book.value?.readPagesCount, newReadPages)
                book.value?.readPagesCount = newReadPages
                _errorMessage.value = null
            }
        } else {
            _errorMessage.value = "Новое значение не может быть меньше предыдущего и больше общего"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun dailyReadingInsert(currentReadPages: Int?, newReadPages: Int) {

        if (currentReadPages != null && newReadPages - currentReadPages != 0) {
            viewModelScope.launch {
//                val dateString = "09-06-2025"
//                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
//                val localDate: LocalDate = LocalDate.parse(dateString, formatter)

                var progressItem = dailyReadingDao.getPagesReadForBookOnDate(LocalDate.now(), book.value?.bookId)

                if (progressItem != null) {
                    val updatedReading = DailyReading(
                        dataId = progressItem.dataId,
                        readDate = progressItem.readDate,
                        bookId = book.value?.bookId ?: 0L,
                        countPage = progressItem.countPage + (newReadPages - currentReadPages)
                    )
                    dailyReadingDao.update(updatedReading)
                }
                else {
                    val progressItemNew = DailyReading(
                        countPage = newReadPages - currentReadPages,
                        bookId = book.value!!.bookId,
                        readDate = LocalDate.now()
                    )
                    dailyReadingDao.insert(progressItemNew)
                }
            }
        }
    }

    fun updateTask() {
        viewModelScope.launch {
            bookDao.update(book.value!!)
            _navigateToDetail.value = true
        }
    }

    fun onNavigatedToList() {
        _navigateToDetail.value = false
    }
}