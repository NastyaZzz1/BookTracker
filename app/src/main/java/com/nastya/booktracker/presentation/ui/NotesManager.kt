package com.nastya.booktracker.presentation.ui

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.nastya.booktracker.R
import com.nastya.booktracker.domain.model.Highlight
import com.nastya.booktracker.presentation.ui.epubReader.EpubReaderViewModel
import org.readium.r2.navigator.SelectableNavigator
import org.readium.r2.shared.publication.Locator

class NotesManager(
    private val viewModel: EpubReaderViewModel,
    private val context: Context,
    private val inflater: LayoutInflater,
    private val navigatorFragment: () -> SelectableNavigator?
) {
    suspend fun showAnnotationPopup(highlight: Highlight?) {
        if (highlight != null) {
            showAnnotationDialog(highlight, null)
        } else {
            navigatorFragment()?.currentSelection()?.let { selection ->
                showAnnotationDialog(null, selection.locator)
                navigatorFragment()?.clearSelection()
            }
        }
    }

    private fun showAnnotationDialog(highlight: Highlight?, locator: Locator? = null) {
        val dialogView = inflater.inflate(R.layout.annotation_dialog, null)
        val tvQuoteText = dialogView.findViewById<TextView>(R.id.tvQuoteText)
        val etNote = dialogView.findViewById<TextInputEditText>(R.id.etNote)

        tvQuoteText?.text = when {
            highlight != null -> viewModel.getLocator(highlight).text.highlight
            locator != null -> locator.text.highlight
            else -> ""
        }
        etNote.setText(highlight?.annotation ?: "")

        MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сохранить") { _, _ ->
                val noteText = etNote?.text.toString()
                if (noteText.isNotEmpty()) {
                    viewModel.saveAnnotation(noteText, highlight, locator)
                }
            }
            .setCancelable(false)
            .create()
            .show()
    }
}