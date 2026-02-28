package com.nastya.booktracker.presentation.ui.bookNotes

import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.nastya.booktracker.databinding.NoteItemBinding
import com.nastya.booktracker.domain.model.Highlight
import com.nastya.booktracker.domain.model.LocatorDto

class NoteItemAdapter(
    private val onItemClick: (Highlight) -> Unit
): ListAdapter<Highlight, NoteItemAdapter.NoteItemViewHolder>(NoteDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteItemViewHolder {
        return NoteItemViewHolder.inflateFrom(parent)
    }

    override fun onBindViewHolder(holder: NoteItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onItemClick)
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

        fun bind(
            item: Highlight?,
            onItemClick: (Highlight) -> Unit
        ) {
            item?.let { note ->
                val text = note.locatorJson
                    .let { Gson().fromJson(it, LocatorDto::class.java) }
                    .toLocator()
                    .text.highlight

                val spannable = SpannableString(text)

                when (note.style) {
                    Highlight.Style.HIGHLIGHT -> {
                        val transparentTint = (note.tint and 0x00FFFFFF) or (0x4C shl 24)
                        spannable.setSpan(
                            BackgroundColorSpan(transparentTint),
                            0, text!!.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    Highlight.Style.UNDERLINE -> {
                        spannable.setSpan(
                            UnderlineSpan(),
                            0, text!!.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        spannable.setSpan(
                            ForegroundColorSpan(note.tint),
                            0, text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                binding.textNote.text = spannable
                binding.annotation.text = note.annotation.ifEmpty { "" }
                binding.root.setOnClickListener { onItemClick(note) }
            }
        }
    }
}