package com.nastya.booktracker

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class EditBookViewModel(bookId: Long, val bookDao: BookDao, val dailyReadingDao: DailyReadingDao) : ViewModel() {
    val book = bookDao.get(bookId)
    private val _navigateToList = MutableLiveData<Boolean>(false)
    val navigateToList: LiveData<Boolean>
        get() = _navigateToList

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun onAllPagesCountChanged(bookPage: Int){
        book.value?.allPagesCount = bookPage
    }

    fun onBookNameChanged(bookName: String){
        book.value?.bookName = bookName
    }

    fun onBookAuthorChanged(bookAuthor: String){
        book.value?.bookAuthor = bookAuthor
    }

    fun onBookDescChanged(bookDesc: String){
        book.value?.description = bookDesc
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onReadPagesCountChanged(newReadPages: Int) {
        if (book.value?.readPagesCount?.let { it <= newReadPages } == true) {
            dailyReadingInsert(book.value?.readPagesCount, newReadPages)
            book.value?.readPagesCount = newReadPages
            _errorMessage.value = null
        } else {
            _errorMessage.value = "Новое значение не может быть меньше предыдущего"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun dailyReadingInsert(currentReadPages: Int?, newReadPages: Int) {
        if (currentReadPages != null && newReadPages - currentReadPages != 0) {
            viewModelScope.launch {
                val progressItem = dailyReadingDao.get(LocalDate.now())

                if (progressItem != null) {
                    Log.d("DailyReading", "Запись найдена " + progressItem.countPage.toString() + " " + progressItem.readDate)
                    progressItem.countPage += newReadPages - currentReadPages
                    progressItem.readDate = LocalDate.now()
                    dailyReadingDao.update(progressItem)
                    Log.d("DailyReading", "Запись обновлена " + progressItem.countPage.toString() + " " + progressItem.readDate)
                }
                else {
                    val progressItemNew = DailyReading(
                        countPage = newReadPages - currentReadPages,
                        readDate = LocalDate.now())
                    dailyReadingDao.insert(progressItemNew)
                    Log.d("DailyReading", "Запись создана " + progressItemNew.countPage.toString() + " " + progressItemNew.readDate)
                }
            }
        }
    }

    fun onBookImgChanged(bookImg: String){
        book.value?.imageUrl = bookImg
    }

    fun updateTask() {
        viewModelScope.launch {
            bookDao.update(book.value!!)
            _navigateToList.value = true
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            bookDao.delete(book.value!!)
            _navigateToList.value = true
        }
    }

    fun onNavigatedToList() {
        _navigateToList.value = false
    }

}