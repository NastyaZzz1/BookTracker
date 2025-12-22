package com.nastya.booktracker.presentation.ui.epubReader

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.nastya.booktracker.R
import com.nastya.booktracker.data.local.database.BookDatabase
import com.nastya.booktracker.databinding.FragmentEpubReaderBinding
import com.nastya.booktracker.domain.model.LocatorDto
import com.nastya.booktracker.presentation.ui.EpubRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import kotlin.math.roundToInt

class EpubReaderFragment : Fragment() {
    private var _binding: FragmentEpubReaderBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EpubReaderViewModel

    private val SAVE_INTERVAL_MS = 3000L
    private var lastSavedAt = 0L
    private var lastLocator: Locator? = null
    private var percentRead: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEpubReaderBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookPath = EpubReaderFragmentArgs.fromBundle(requireArguments()).bookPath
        val bookId = EpubReaderFragmentArgs.fromBundle(requireArguments()).bookId

        Log.d("fragment", bookId.toString())

        val application = requireNotNull(this.activity).application
        val dao = BookDatabase.Companion.getInstance(application).bookDao
        val viewModelFactory = EpubReaderViewModelFactory(dao, bookId)
        viewModel = ViewModelProvider(this, viewModelFactory)[EpubReaderViewModel::class.java]

        lifecycleScope.launch {
            val epubRepository = EpubRepository(requireContext())
            val publication = epubRepository.extractMetadata(bookPath)

            val locator = viewModel.book
                .firstOrNull()
                ?.locatorJson
                ?.takeIf { it.isNotEmpty() }
                ?.let { Gson().fromJson(it, LocatorDto::class.java) }
                ?.toLocator()

            displayPublication(publication, locator)
        }
    }

    private fun displayPublication(
        publication: Publication,
        initialLocator: Locator?
    ) {
        val fragmentFactory = EpubNavigatorFragment.Companion.createFactory(
            publication = publication,
            initialLocator = initialLocator,
            listener = null,
            paginationListener = null,
            config = EpubNavigatorFragment.Configuration()
        )

        childFragmentManager.fragmentFactory = fragmentFactory

        childFragmentManager.beginTransaction()
            .replace(R.id.reader_container, EpubNavigatorFragment::class.java, null, "EPUB_NAVIGATOR")
            .commitNow()

        val navigatorFragment =
            childFragmentManager.findFragmentByTag("EPUB_NAVIGATOR")
                    as EpubNavigatorFragment

        setPrecentRead(navigatorFragment)
    }

    private fun setPrecentRead(navigatorFragment: EpubNavigatorFragment) {
        viewLifecycleOwner.lifecycleScope.launch {
            navigatorFragment.currentLocator.collect { locator ->
                percentRead = ((locator.locations.totalProgression ?: 0.0) * 100).roundToInt()
                val lastHref = navigatorFragment.publication.readingOrder.last().href
                percentRead =
                    if (locator.href == lastHref && percentRead >= 99) 100
                    else percentRead

                binding.countPage.text = "$percentRead%"

                lastLocator = locator
                maybeSaveProgress()
//                viewModel.saveProgressToDb(lastLocator, percentRead)
            }
        }
    }

    private fun maybeSaveProgress() {
        val now = System.currentTimeMillis()
        if (now - lastSavedAt > SAVE_INTERVAL_MS) {
            viewModel.saveProgressToDb(lastLocator, percentRead)
            lastSavedAt = now
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveProgressToDb(lastLocator, percentRead)
    }
}