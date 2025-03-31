package com.nastya.booktracker

import androidx.recyclerview.widget.DiffUtil

class BookDiffItemCallback : DiffUtil.ItemCallback<Book>() {
    override fun areItemsTheSame(oldItem: Book, newItem: Book)
        = (oldItem.bookId == newItem.bookId)

    override fun areContentsTheSame(oldItem: Book, newItem: Book) = (oldItem == newItem)
}