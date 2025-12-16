package com.nastya.booktracker.presentation.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nastya.booktracker.R
import kotlinx.coroutines.*
import org.readium.r2.shared.publication.Publication
import androidx.lifecycle.lifecycleScope
import com.nastya.booktracker.databinding.FragmentEpubReaderBinding
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import kotlin.math.roundToInt

class EpubReaderFragment : Fragment() {
    private var _binding: FragmentEpubReaderBinding? = null
    private val binding get() = _binding!!

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

        lifecycleScope.launch {
            val epubRepository = EpubRepository(requireContext())
            val publication = epubRepository.extractMetadata(bookPath)
            displayPublication(publication)
        }
    }

    private fun displayPublication(publication: Publication) {
        val fragmentFactory = EpubNavigatorFragment.createFactory(
            publication = publication,
            initialLocator = null,
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

        viewLifecycleOwner.lifecycleScope.launch {
            navigatorFragment.currentLocator.collect { locator ->
                var percentRead = ((locator.locations.totalProgression ?: 0.0) * 100).roundToInt()
                val lastHref = navigatorFragment.publication.readingOrder.last().href
                percentRead = if (locator.href == lastHref && percentRead >= 99) 100 else percentRead

                binding.countPage.text = "$percentRead%"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}