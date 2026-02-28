package com.nastya.booktracker.presentation.ui.epubReader

import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.nastya.booktracker.presentation.ui.HighlightsManager
import com.nastya.booktracker.presentation.ui.NotesManager
import com.nastya.booktracker.R
import com.nastya.booktracker.data.local.database.BookDatabase
import com.nastya.booktracker.databinding.FragmentEpubReaderBinding
import com.nastya.booktracker.domain.model.Highlight
import com.nastya.booktracker.domain.model.LocatorDto
import com.nastya.booktracker.presentation.ui.ThemeManager
import com.nastya.booktracker.presentation.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.readium.r2.navigator.DecorableNavigator
import org.readium.r2.navigator.Decoration
import org.readium.r2.navigator.ExperimentalDecorator
import org.readium.r2.navigator.SelectableNavigator
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.util.BaseActionModeCallback
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import kotlin.math.roundToInt

@OptIn(ExperimentalDecorator::class, ExperimentalReadiumApi::class)
class EpubReaderFragment : Fragment() {
    private var _binding: FragmentEpubReaderBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EpubReaderViewModel
    private var navigatorFragment: EpubNavigatorFragment? = null

    private lateinit var highlightsManager: HighlightsManager
    private lateinit var notesManager: NotesManager
    private lateinit var themeManager: ThemeManager

    private val decorationListener by lazy { DecorationListener() }

    private val saveInternalMs = 3000L
    private var lastSavedAt = 0L
    private var lastLocator: Locator? = null
    private var percentRead: Int = 0

    private lateinit var bookPath: String
    private var bookId: Long = 0L
    private var pendingHighlightId: Long? = null

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

    fun getBookId() = bookId

    override fun onCreate(savedInstanceState: Bundle?) {
        arguments?.let {
            bookPath = EpubReaderFragmentArgs.fromBundle(it).bookPath
            bookId = EpubReaderFragmentArgs.fromBundle(it).bookId
            pendingHighlightId = EpubReaderFragmentArgs.fromBundle(it).highlightId
        }
        initViewModel()
        viewModel.publication.value?.let { publication ->
            childFragmentManager.fragmentFactory =
                EpubNavigatorFragment.createFactory(
                publication = publication,
                initialLocator = null,
                listener = tapListener,
                paginationListener = null,
                config = EpubNavigatorFragment.Configuration(
                    selectionActionModeCallback = SelectionActionModeCallback()
                )
            )
        }
        super.onCreate(savedInstanceState)
    }

    private fun initViewModel() {
        val application = requireNotNull(this.activity).application
        val bookDao = BookDatabase.getInstance(application).bookDao
        val dailyReadingDao = BookDatabase.getInstance(application).dailyReadingDao
        val highlightDao = BookDatabase.getInstance(application).highlightDao

        val viewModelFactory = EpubReaderViewModelFactory(bookDao, dailyReadingDao, highlightDao, bookId)
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
        setupObservers(savedInstanceState)
        initManagers()

        pendingHighlightId?.let {
            navigateToHighlight(it)
            pendingHighlightId = null
        }
    }

    private fun initManagers() {
        highlightsManager = HighlightsManager(
            viewModel = viewModel,
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            navigatorFragment = { navigatorFragment },
            showAnnotationPopup = { highlight ->
                lifecycleScope.launch {
                    notesManager.showAnnotationPopup(highlight)
                }
            }
        )

        notesManager = NotesManager(
            viewModel = viewModel,
            context = requireContext(),
            inflater = layoutInflater,
            navigatorFragment = { navigatorFragment as? SelectableNavigator }
        )

        themeManager = ThemeManager { navigatorFragment }
    }

    private fun setupObservers(savedInstanceState: Bundle?) {
        if(savedInstanceState == null) {
            observePublication()
        } else {
            restoreNavigatorFragment()
        }
        observeDecoration()
    }

    private fun observeDecoration() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.highlightDecorations.collect { decorations ->
                while (navigatorFragment == null) { delay(100) }
                (navigatorFragment as? DecorableNavigator)?.apply {
                    applyDecorations(decorations, "highlights")
                    addDecorationListener("highlights", decorationListener)
                }
            }
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
        return viewModel.book.firstOrNull()?.locatorJson
            ?.takeIf { it.isNotEmpty() }
            ?.let { Gson().fromJson(it, LocatorDto::class.java) }
            ?.toLocator()
    }

    private fun displayPublication(publication: Publication, initialLocator: Locator?) {
        childFragmentManager.fragmentFactory = EpubNavigatorFragment.createFactory(
            publication = publication,
            initialLocator = initialLocator,
            listener = tapListener,
            paginationListener = null,
            config = EpubNavigatorFragment.Configuration(
                selectionActionModeCallback = SelectionActionModeCallback()
            )
        )

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

    private fun navigateToHighlight(highlightId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            while (navigatorFragment == null) { delay(100) }
            val highlight = viewModel.getHighlightById(highlightId)
            if (highlight != null) {
                val locator = viewModel.getLocator(highlight)
                navigatorFragment?.go(locator, animated = true)
            }
        }
    }

    // ==================== CALLBACKS ====================

    private inner class SelectionActionModeCallback() : BaseActionModeCallback() {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_action_mode, menu)
            if (navigatorFragment is DecorableNavigator) {
                menu.findItem(R.id.quote_highlighting).isVisible = true
                menu.findItem(R.id.note_underline).isVisible = true
            }
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.quote_highlighting ->
                    this@EpubReaderFragment.highlightsManager.showHighlightPopupWithStyle(Highlight.Style.HIGHLIGHT)
                R.id.note_underline ->
                    this@EpubReaderFragment.highlightsManager.showHighlightPopupWithStyle(Highlight.Style.UNDERLINE)
                else -> return false
            }
            mode.finish()
            return true
        }
    }

    private inner class DecorationListener : DecorableNavigator.Listener {
        override fun onDecorationActivated(event: DecorableNavigator.OnActivatedEvent): Boolean {
            val highlightId = event.decoration.extras["id"] as? Long

            if (highlightId != null) {
                event.rect?.let { rect ->
                    val style = when (event.decoration.style) {
                        is Decoration.Style.Highlight -> Highlight.Style.HIGHLIGHT
                        is Decoration.Style.Underline -> Highlight.Style.UNDERLINE
                        else -> null
                    }
                    style?.let {
                        highlightsManager.showHighlightPopup(rect, it, highlightId)
                    }
                }
            }
            return true
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setSettingsBottomDialog() {
        themeManager.showThemeDialog(layoutInflater, requireContext())
    }

    private fun maybeSaveProgress() {
        val now = System.currentTimeMillis()
        if (now - lastSavedAt > saveInternalMs) {
            viewModel.saveProgressToDb(lastLocator, percentRead)
            lastSavedAt = now
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            bookPath = savedInstanceState.getString("bookPath") ?: bookPath
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("bookPath", bookPath)
        super.onSaveInstanceState(outState)
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