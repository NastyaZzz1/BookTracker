package com.nastya.booktracker.presentation.ui

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.jackandphantom.carouselrecyclerview.CarouselLayoutManager
import com.jackandphantom.carouselrecyclerview.CarouselRecyclerview
import com.nastya.booktracker.R
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.FontFamily
import org.readium.r2.navigator.preferences.Theme
import org.readium.r2.shared.ExperimentalReadiumApi

@OptIn(ExperimentalReadiumApi::class)
class ThemeManager(private val navigatorFragment: () -> EpubNavigatorFragment?) {
    private lateinit var themeButtons: List<MaterialButton>

    private var currentTheme: Theme = Theme.LIGHT
    private var currentFontSize: Double = 1.0
    private var currentFontFamily: FontFamily = FontFamily.SERIF
    private var currentFontIndex: Int = 1

    private val MAX_FONT_SIZE = 2.5
    private val MIN_FONT_SIZE = 0.5
    private val STEP = 0.25

    private val fonts = listOf(
        "Sans Serif" to FontFamily.SANS_SERIF,
        "Serif" to FontFamily.SERIF,
        "Mono" to FontFamily.MONOSPACE,
        "Cursive" to FontFamily.CURSIVE
    )
    
    @RequiresApi(Build.VERSION_CODES.M)
    fun showThemeDialog(inflater: LayoutInflater, context: Context) {
        val viewDialog = inflater.inflate(R.layout.settings_bottom_sheet_dialog, null)
        BottomSheetDialog(context).apply {
            setupThemeButtons(viewDialog, context)
            setupFontSizeButtons(viewDialog)
            setupFontFamily(viewDialog)

            setCancelable(true)
            setCanceledOnTouchOutside(true)
            setContentView(viewDialog)
            show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupThemeButtons(viewDialog: View, context: Context) {
        val whiteBtn = viewDialog.findViewById<MaterialButton>(R.id.white_style)
        val sepiaBtn = viewDialog.findViewById<MaterialButton>(R.id.sepia_style)
        val blackBtn = viewDialog.findViewById<MaterialButton>(R.id.black_style)

        themeButtons = listOf(whiteBtn, sepiaBtn, blackBtn)

        whiteBtn.setOnClickListener {
            viewDialog.setBackgroundColor(context.getColor(R.color.white))
            selectButton(whiteBtn)
            currentTheme = Theme.LIGHT
            applySettings()
        }
        sepiaBtn.setOnClickListener {
            viewDialog.setBackgroundColor(context.getColor(R.color.sepia))
            selectButton(sepiaBtn)
            currentTheme = Theme.SEPIA
            applySettings()
        }
        blackBtn.setOnClickListener {
            viewDialog.setBackgroundColor(context.getColor(R.color.black))
            selectButton(blackBtn)
            currentTheme = Theme.DARK
            applySettings()
        }
    }

    private fun setupFontSizeButtons(viewDialog: View) {
        val minusBtn = viewDialog.findViewById<MaterialButton>(R.id.btnMinus)
        val plusBtn = viewDialog.findViewById<MaterialButton>(R.id.btnPlus)

        minusBtn.setOnClickListener {
            val newSize = (currentFontSize - STEP).coerceAtLeast(MIN_FONT_SIZE)
            if (newSize != currentFontSize) {
                currentFontSize = newSize
                applySettings()
            }
        }
        plusBtn.setOnClickListener {
            val newSize = (currentFontSize + STEP).coerceAtMost(MAX_FONT_SIZE)
            if (newSize != currentFontSize) {
                currentFontSize = newSize
                applySettings()
            }
        }
    }

    fun setupFontFamily(viewDialog: View) {
        val carousel = viewDialog.findViewById<CarouselRecyclerview>(R.id.carouselRecyclerview)
        val fontNames = fonts.map { it.first }

        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                object : RecyclerView.ViewHolder(TextView(parent.context)) {}

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                (holder.itemView as TextView).apply {
                    text = fontNames[position]
                    textSize = 18f
                    gravity = Gravity.CENTER
                    setPadding(48, 24, 24, 24)

                    if (position == currentFontIndex) {
                        setTextColor("#8B5E3C".toColorInt())
                        setTypeface(typeface, Typeface.BOLD)
                    } else {
                        setTextColor("#666666".toColorInt())
                        setTypeface(typeface, Typeface.NORMAL)
                    }
                }
            }

            override fun getItemCount() = fonts.size
        }

        carousel.adapter = adapter
        carousel.set3DItem(true)
        carousel.setAlpha(true)
        carousel.setIntervalRatio(0.7f)

        carousel.post {
            carousel.scrollToPosition(currentFontIndex)
        }

        carousel.setItemSelectListener(object : CarouselLayoutManager.OnSelected {
            override fun onItemSelected(position: Int) {
                if (currentFontIndex != position) {
                    currentFontIndex = position
                    currentFontFamily = fonts[position].second
                    applySettings()
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun applySettings() {
        navigatorFragment()?.submitPreferences(EpubPreferences(
            theme = currentTheme,
            fontSize = currentFontSize,
            fontFamily = currentFontFamily
        ))
    }

    private fun selectButton(selectedBtn: MaterialButton) {
        themeButtons.forEach { it.isChecked = false }
        selectedBtn.isChecked = true
    }
}