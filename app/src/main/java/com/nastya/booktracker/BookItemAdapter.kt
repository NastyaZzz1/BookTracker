package com.nastya.booktracker

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class BookItemAdapter : RecyclerView.Adapter<BookItemAdapter.BookItemViewHolder>() {

    var data = listOf<Book>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookItemViewHolder {
        return BookItemViewHolder.inflateFrom(parent)
    }

    override fun onBindViewHolder(holder: BookItemViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    class BookItemViewHolder(val rootView: CardView)
        : RecyclerView.ViewHolder(rootView) {

        val bookName = rootView.findViewById<TextView>(R.id.book_name)
        val bookAuthor = rootView.findViewById<TextView>(R.id.book_author)

        companion object {
            fun inflateFrom(parent: ViewGroup): BookItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.book_item, parent, false) as CardView
                return BookItemViewHolder(view)
            }
        }
        fun bind(item: Book) {
            bookName.text = item.bookName
            bookAuthor.text = item.bookAuthor
        }
    }
}