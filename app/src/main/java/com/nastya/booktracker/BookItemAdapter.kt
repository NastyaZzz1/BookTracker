package com.nastya.booktracker

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nastya.booktracker.databinding.BookItemBinding

class BookItemAdapter(val clickListener: (bookId: Long) -> Unit) :
    ListAdapter<Book, BookItemAdapter.BookItemViewHolder>(BookDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookItemViewHolder {
        return BookItemViewHolder.inflateFrom(parent)
    }

    override fun onBindViewHolder(holder: BookItemViewHolder, position: Int) {
        val item  = getItem(position)
        holder.bind(item, clickListener)
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
        fun bind(item: Book?, clickListener: (bookId: Long) -> Unit) {
            item?.let { book ->
                val progress = (book.readPagesCount * 100 ) / book.allPagesCount;
                binding.linProgressBar.setProgressCompat(0, false)
                binding.book = book
                binding.root.setOnClickListener { clickListener(book.bookId) }
                binding.linProgressBar.progress = progress;
                binding.linProgressText.text = "$progress%";
                binding.linProgressBar.isIndeterminate = false;

                binding.bookImage.load(book.imageUrl) {
                    crossfade(true)
//                    placeholder(R.drawable.placeholder)
//                      error(R.drawable.ic_error)
                }
            } ?: run {
                binding.bookName.text = "No data"
//                binding.bookImage.setImageResource(R.drawable.placeholder)
            }
        }
    }
}