package com.nastya.booktracker.presentation.ui.bookNotes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.nastya.booktracker.databinding.NoteItemBinding
import com.nastya.booktracker.domain.model.Highlight
import com.nastya.booktracker.domain.model.LocatorDto

class NoteItemAdapter(

): ListAdapter<Highlight, NoteItemAdapter.NoteItemViewHolder>(NoteDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteItemViewHolder {
        return NoteItemViewHolder.inflateFrom(parent)
    }

    override fun onBindViewHolder(holder: NoteItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class NoteItemViewHolder(val binding: NoteItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflateFrom(parent: ViewGroup): NoteItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = NoteItemBinding.inflate(layoutInflater, parent, false)
                return NoteItemViewHolder(binding)
            }
        }

        fun bind(item: Highlight?) {
            item?.let { note ->
                binding.textNote.text = note.locatorJson
                    .let { Gson().fromJson(it, LocatorDto::class.java) }
                    .toLocator()
                    .text.highlight
            }
        }
    }
}