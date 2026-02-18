package com.nastya.booktracker.presentation.ui.epubReader

import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.nastya.booktracker.R
import com.nastya.booktracker.data.local.database.BookDatabase
import com.nastya.booktracker.databinding.FragmentEpubReaderBinding
import com.nastya.booktracker.domain.model.LocatorDto
import com.nastya.booktracker.presentation.ui.main.MainActivity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.Theme
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import kotlin.math.roundToInt

class EpubReaderFragment : Fragment() {
    private var _binding: FragmentEpubReaderBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EpubReaderViewModel
    private var navigatorFragment: EpubNavigatorFragment? = null
    private lateinit var themeButtons: List<MaterialButton>

    private val saveInternalMs = 3000L
    private var lastSavedAt = 0L
    private var lastLocator: Locator? = null
    private var percentRead: Int = 0

    private lateinit var bookPath: String
    private var bookId: Long = 0L

    private val tapListener = object : EpubNavigatorFragment.Listener {
        override fun onTap(point: PointF): Boolean {
            val activity = requireActivity() as MainActivity
            val toolbarAlpha = activity.binding.toolbar.alpha
            if (toolbarAlpha > 0f) {
                activity.hideSystemUi()
            } else {
                activity.showSystemUi()
            }
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        arguments?.let {
            bookPath = EpubReaderFragmentArgs.fromBundle(it).bookPath
            bookId = EpubReaderFragmentArgs.fromBundle(it).bookId
        }
        initViewModel()

        viewModel.publication.value?.let { publication ->
            childFragmentManager.fragmentFactory =
                EpubNavigatorFragment.createFactory(
                    publication = publication,
                    initialLocator = null,
                    listener = tapListener,
                    paginationListener = null,
                    config = EpubNavigatorFragment.Configuration()
                )
        }
        super.onCreate(savedInstanceState)
    }

    private fun initViewModel() {
        val application = requireNotNull(this.activity).application
        val bookDao = BookDatabase.getInstance(application).bookDao
        val dailyReadingDao = BookDatabase.getInstance(application).dailyReadingDao

        val viewModelFactory = EpubReaderViewModelFactory(bookDao, dailyReadingDao, bookId)
        viewModel = ViewModelProvider(this, viewModelFactory)[EpubReaderViewModel::class.java]

        viewModel.loadPublication(requireContext(), bookPath)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEpubReaderBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(savedInstanceState == null) {
            observePublication()
        } else {
            restoreNavigatorFragment()
        }
    }

    private fun observePublication() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.publication.collect { publication ->
                if (publication != null && navigatorFragment == null) {
                    createNewNavigatorFragment(publication)
                }
            }
        }
    }

    private fun restoreNavigatorFragment() {
        navigatorFragment =
            childFragmentManager.findFragmentByTag("EPUB_NAVIGATOR")
                    as EpubNavigatorFragment

        navigatorFragment?.let { setPrecentRead(it) }
    }

    private suspend fun createNewNavigatorFragment(publication: Publication) {
        val initialLocator = getInitialLocator()
        displayPublication(publication, initialLocator)
    }

    private suspend fun getInitialLocator(): Locator? {
        return viewModel.book
            .firstOrNull()
            ?.locatorJson
            ?.takeIf { it.isNotEmpty() }
            ?.let { Gson().fromJson(it, LocatorDto::class.java) }
            ?.toLocator()
    }

    private fun displayPublication(
        publication: Publication,
        initialLocator: Locator?
    ) {
        val fragmentFactory = EpubNavigatorFragment.createFactory(
            publication = publication,
            initialLocator = initialLocator,
            listener = tapListener,
            paginationListener = null,
            config = EpubNavigatorFragment.Configuration()
        )

        childFragmentManager.fragmentFactory = fragmentFactory

        childFragmentManager.beginTransaction()
            .replace(R.id.reader_container, EpubNavigatorFragment::class.java, null, "EPUB_NAVIGATOR")
            .commitNow()

        navigatorFragment =
            childFragmentManager.findFragmentByTag("EPUB_NAVIGATOR")
                    as EpubNavigatorFragment

        navigatorFragment?.let { setPrecentRead(it) }
    }

    private fun setPrecentRead(navigatorFragment: EpubNavigatorFragment) {
        viewLifecycleOwner.lifecycleScope.launch {
            navigatorFragment.currentLocator.collect { locator ->
                percentRead = ((locator.locations.totalProgression ?: 0.0) * 100).roundToInt()
                val lastHref = navigatorFragment.publication.readingOrder.last().href
                percentRead =
                    if (locator.href == lastHref && percentRead >= 99) 100
                    else percentRead

                if (binding.percentagesProgress.text != "$percentRead%") {
                    binding.percentagesProgress.text = "$percentRead%"
                }
                lastLocator = locator
                maybeSaveProgress()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setSettingsBottomDialog() {
        val viewDialog = layoutInflater.inflate(R.layout.settings_bottom_sheet_dialog, null)
        val dialog = BottomSheetDialog(requireContext())

        setStyleButtons(viewDialog)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(viewDialog)
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setStyleButtons(viewDialog: View) {
        val whiteBtn = viewDialog.findViewById<MaterialButton>(R.id.white_style)
        val sepiaBtn = viewDialog.findViewById<MaterialButton>(R.id.sepia_style)
        val blackBtn = viewDialog.findViewById<MaterialButton>(R.id.black_style)

        themeButtons = listOf(whiteBtn, sepiaBtn, blackBtn)

        whiteBtn.setOnClickListener {
            viewDialog.setBackgroundColor(requireContext().getColor(R.color.white))
            selectButton(whiteBtn)
            applyTheme(Theme.LIGHT)
        }
        sepiaBtn.setOnClickListener {
            viewDialog.setBackgroundColor(requireContext().getColor(R.color.sepia))
            selectButton(sepiaBtn)
            applyTheme(Theme.SEPIA)
        }
        blackBtn.setOnClickListener {
            viewDialog.setBackgroundColor(requireContext().getColor(R.color.black))
            selectButton(blackBtn)
            applyTheme(Theme.DARK)
        }
    }

    private fun applyTheme(theme: Theme) {
        navigatorFragment?.submitPreferences(EpubPreferences(theme = theme))
    }

    private fun selectButton(selectedBtn: MaterialButton) {
        themeButtons.forEach { it.isChecked = false }
        selectedBtn.isChecked = true
    }

    private fun maybeSaveProgress() {
        val now = System.currentTimeMillis()
        if (now - lastSavedAt > saveInternalMs) {
            viewModel.saveProgressToDb(lastLocator, percentRead)
            lastSavedAt = now
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("bookPath", bookPath)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            bookPath = savedInstanceState.getString("bookPath") ?: bookPath
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        viewModel.saveProgressToDb(lastLocator, percentRead)

        val elapsedTime = ((requireActivity() as? MainActivity)
            ?.resetTimerFromReader()
            ?: 0L) / 1000
        viewModel.saveReadingTime(elapsedTime)
    }

    override fun onDestroy() {
        super.onDestroy()
        navigatorFragment = null
    }
}