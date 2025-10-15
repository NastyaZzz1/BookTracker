package com.nastya.booktracker.presentation.ui.bookList

import androidx.recyclerview.widget.DiffUtil
import com.nastya.booktracker.domain.model.Book

class BookDiffItemCallback : DiffUtil.ItemCallback<Book>() {
    override fun areItemsTheSame(oldItem: Book, newItem: Book)
        = (oldItem.bookId == newItem.bookId)

    override fun areContentsTheSame(oldItem: Book, newItem: Book) = (oldItem == newItem)
}