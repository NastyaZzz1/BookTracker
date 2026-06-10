package com.nastya.booktracker.presentation.ui.bookList

import androidx.recyclerview.widget.DiffUtil
import com.nastya.booktracker.domain.model.Book

class BookDiffItemCallback : DiffUtil.ItemCallback<Book>() {
    override fun areItemsTheSame(oldItem: Book, newItem: Book)
        = (oldItem.bookId == newItem.bookId)

    override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
        return oldItem.bookId == newItem.bookId &&
                oldItem.bookName == newItem.bookName &&
                oldItem.bookAuthor == newItem.bookAuthor &&
                oldItem.progress == newItem.progress &&
                oldItem.isFavorite == newItem.isFavorite &&
                oldItem.imageData.contentEquals(newItem.imageData)
    }
}