package com.nastya.booktracker.presentation.ui.bookNotes

import androidx.recyclerview.widget.DiffUtil
import com.nastya.booktracker.domain.model.Highlight

class NoteDiffItemCallback: DiffUtil.ItemCallback<Highlight>() {
    override fun areItemsTheSame(oldItem: Highlight, newItem: Highlight)
            = (oldItem.id == newItem.id)

    override fun areContentsTheSame(oldItem: Highlight, newItem: Highlight) = (oldItem == newItem)
}