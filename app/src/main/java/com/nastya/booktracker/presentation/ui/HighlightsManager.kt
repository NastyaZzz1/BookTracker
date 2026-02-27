package com.nastya.booktracker.presentation.ui

import android.graphics.Color
import android.graphics.RectF
import android.view.ActionMode
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.annotation.ColorInt
import androidx.lifecycle.LifecycleCoroutineScope
import com.nastya.booktracker.R
import com.nastya.booktracker.domain.model.Highlight
import com.nastya.booktracker.presentation.ui.epubReader.EpubReaderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.readium.r2.navigator.SelectableNavigator
import org.readium.r2.navigator.epub.EpubNavigatorFragment

class HighlightsManager(
    private val viewModel: EpubReaderViewModel,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val navigatorFragment: () -> EpubNavigatorFragment?,
    private val showAnnotationPopup: (Highlight?) -> Unit
) {
    private var popupWindow: PopupWindow? = null
    private var mode: ActionMode? = null

    private val highlightTints = mapOf(
        R.id.red to Color.rgb(247, 124, 124),
        R.id.green to Color.rgb(173, 247, 123),
        R.id.blue to Color.rgb(124, 198, 247),
        R.id.yellow to Color.rgb(249, 239, 125),
        R.id.purple to Color.rgb(182, 153, 255)
    )

    fun showHighlightPopup(rect: RectF, style: Highlight.Style, highlightId: Long? = null) {
        lifecycleScope.launch {
            if (popupWindow?.isShowing == true) return@launch

            val context = navigatorFragment()?.context ?: return@launch
            val inflater = LayoutInflater.from(context)

            val popupView = inflater.inflate(
                R.layout.view_action_mode,
                null,
                false
            ).apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
            }

            popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                isFocusable = true
            }

            val x = rect.left
            val y = rect.top
            popupWindow?.showAtLocation(popupView, Gravity.NO_GRAVITY, x.toInt(), y.toInt())

            popupView.run {
                findViewById<View>(R.id.notch).run {
                    setX(rect.left * 2)
                }

                val highlight = highlightId?.let {
                    viewModel.getHighlightById(it)
                }

                fun selectOption(view: View) {
                    val tint = highlightTints[view.id] ?: return
                    lifecycleScope.launch {
                        selectHighlightTint(highlight, style, tint)
                    }
                }

                findViewById<View>(R.id.red).setOnClickListener(::selectOption)
                findViewById<View>(R.id.green).setOnClickListener(::selectOption)
                findViewById<View>(R.id.blue).setOnClickListener(::selectOption)
                findViewById<View>(R.id.yellow).setOnClickListener(::selectOption)
                findViewById<View>(R.id.purple).setOnClickListener(::selectOption)

                findViewById<View>(R.id.annotation).run {
                    visibility = if (style == Highlight.Style.UNDERLINE) View.VISIBLE else View.GONE
                    setOnClickListener {
                        popupWindow?.dismiss()
                        lifecycleScope.launch {
                            showAnnotationPopup(highlight)
                        }
                    }
                }

                findViewById<View>(R.id.del).run {
                    visibility = if (highlight != null) View.VISIBLE else View.GONE
                    setOnClickListener {
                        highlightId?.let { viewModel.deleteHighlight(it) }
                        popupWindow?.dismiss()
                        mode?.finish()
                    }
                }
            }
        }
    }

    private suspend fun selectHighlightTint(
        highlight: Highlight? = null,
        style: Highlight.Style,
        @ColorInt tint: Int,
    ) {
        if (highlight != null) {
            viewModel.updateHighlightStyle(highlight, style, tint)
        } else {
            (navigatorFragment() as? SelectableNavigator).let { navigator ->
                navigator?.currentSelection()?.let { selection ->
                    val newHighlightId = viewModel.addHighlight(style, tint, selection.locator)
                    val newHighlight = viewModel.getHighlightById(newHighlightId)
                    if (style == Highlight.Style.UNDERLINE) {
                        showAnnotationPopup(newHighlight)
                    }
                }
                navigator?.clearSelection()
            }
        }
        popupWindow?.dismiss()
        mode?.finish()
    }

    fun showHighlightPopupWithStyle(style: Highlight.Style) {
        lifecycleScope.launch {
            while ((navigatorFragment() as? SelectableNavigator) == null) { delay(100) }
            navigatorFragment()?.currentSelection()?.rect?.let { selectionRect ->
                showHighlightPopup(selectionRect, style)
            }
        }
    }
}