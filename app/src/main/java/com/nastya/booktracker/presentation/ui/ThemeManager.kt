package com.nastya.booktracker.presentation.ui

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.nastya.booktracker.R
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.Theme

class ThemeManager(private val navigatorFragment: () -> EpubNavigatorFragment?) {
    private lateinit var themeButtons: List<MaterialButton>

    @RequiresApi(Build.VERSION_CODES.M)
    fun showThemeDialog(inflater: LayoutInflater, context: Context) {
        val viewDialog = inflater.inflate(R.layout.settings_bottom_sheet_dialog, null)
        BottomSheetDialog(context).apply {
            setupButtons(viewDialog, context)
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            setContentView(viewDialog)
            show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupButtons(viewDialog: View, context: Context) {
        val whiteBtn = viewDialog.findViewById<MaterialButton>(R.id.white_style)
        val sepiaBtn = viewDialog.findViewById<MaterialButton>(R.id.sepia_style)
        val blackBtn = viewDialog.findViewById<MaterialButton>(R.id.black_style)

        themeButtons = listOf(whiteBtn, sepiaBtn, blackBtn)

        whiteBtn.setOnClickListener {
            viewDialog.setBackgroundColor(context.getColor(R.color.white))
            selectButton(whiteBtn)
            applyTheme(Theme.LIGHT)
        }
        sepiaBtn.setOnClickListener {
            viewDialog.setBackgroundColor(context.getColor(R.color.sepia))
            selectButton(sepiaBtn)
            applyTheme(Theme.SEPIA)
        }
        blackBtn.setOnClickListener {
            viewDialog.setBackgroundColor(context.getColor(R.color.black))
            selectButton(blackBtn)
            applyTheme(Theme.DARK)
        }
    }

    private fun applyTheme(theme: Theme) {
        navigatorFragment()?.submitPreferences(EpubPreferences(theme = theme))
    }

    private fun selectButton(selectedBtn: MaterialButton) {
        themeButtons.forEach { it.isChecked = false }
        selectedBtn.isChecked = true
    }
}