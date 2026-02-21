package com.nastya.booktracker.presentation.ui.bookList

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nastya.booktracker.R
import com.nastya.booktracker.databinding.BookItemBinding
import com.nastya.booktracker.domain.model.Book

class BookItemAdapter(
    val onItemClick: (bookId: Long) -> Unit,
    val onFavoriteClick: (bookId: Long) -> Unit
) : ListAdapter<Book, BookItemAdapter.BookItemViewHolder>(BookDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookItemViewHolder {
        return BookItemViewHolder.inflateFrom(parent)
    }

    override fun onBindViewHolder(holder: BookItemViewHolder, position: Int) {
        val item  = getItem(position)
        holder.bind(item, onItemClick, onFavoriteClick)
    }

    class BookItemViewHolder(val binding: BookItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflateFrom(parent: ViewGroup): BookItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = BookItemBinding.inflate(layoutInflater, parent, false)
                return BookItemViewHolder(binding)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: Book?,
                 onItemClick: (bookId: Long) -> Unit,
                 onFavoriteClick: (bookId: Long) -> Unit) {
            item?.let { book ->
                val progress = book.progress
                binding.linProgressBar.progress = progress
                binding.linProgressText.text = "$progress%"

                binding.root.setOnClickListener { onItemClick(book.bookId) }

                binding.bookName.text = book.bookName
                binding.bookAuthor.text = book.bookAuthor

                binding.favBtn.setOnClickListener{
                    book.isFavorite = !book.isFavorite
                    binding.favBtn.setImageResource(
                        if (book.isFavorite) R.drawable.icon_heart
                        else R.drawable.icon_heart_empty
                    )
                    onFavoriteClick(book.bookId)
                }

                binding.favBtn.setImageResource(
                    if (book.isFavorite) R.drawable.icon_heart
                    else R.drawable.icon_heart_empty
                )

                binding.bookImage.load(book.imageData) {
                    crossfade(true)
                }
            } ?: run {
                binding.bookName.text = "No data"
            }
        }
    }
}