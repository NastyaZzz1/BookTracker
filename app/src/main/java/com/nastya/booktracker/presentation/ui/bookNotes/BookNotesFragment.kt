package com.nastya.booktracker.presentation.ui.bookNotes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.nastya.booktracker.R
import com.nastya.booktracker.data.local.database.BookDatabase
import com.nastya.booktracker.databinding.FragmentBookNotesBinding
import com.nastya.booktracker.domain.model.Highlight
import com.nastya.booktracker.presentation.ui.bookDetail.BookDetailFragmentArgs
import kotlinx.coroutines.launch

class BookNotesFragment : Fragment() {
    private var _binding: FragmentBookNotesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BookNotesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookNotesBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookId = BookDetailFragmentArgs.fromBundle(requireArguments()).bookId

        initViewModel(bookId)

        val adapter = NoteItemAdapter { highlight ->
            openReaderAtHighlight(highlight)
        }
        binding.notesList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredHighlights.collect { highlights ->
                adapter.submitList(highlights)
            }
        }
        setupFilterButtons()
    }

    private fun initViewModel(bookId: Long) {
        val application = requireNotNull(this.activity).application
        val highlightDao = BookDatabase.getInstance(application).highlightDao
        val bookDao = BookDatabase.getInstance(application).bookDao
        val viewModelFactory = BookNotesViewModelFactory(highlightDao, bookDao, bookId)
        viewModel = ViewModelProvider(this, viewModelFactory)[BookNotesViewModel::class.java]
    }

    private fun setupFilterButtons() {
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val category = when (checkedId) {
                    R.id.btnAll -> null
                    R.id.btnQuotes -> Highlight.Style.HIGHLIGHT
                    R.id.btnNotes -> Highlight.Style.UNDERLINE
                    else -> null
                }
                viewModel.filterByCategory(category)
            }
        }
    }

    private fun openReaderAtHighlight(highlight: Highlight) {
        lifecycleScope.launch {
            val bundle = Bundle().apply {
                putString("bookPath", viewModel.getBookPath())
                putLong("bookId", highlight.bookId)
                putLong("highlightId", highlight.id)
            }
            findNavController().navigate(
                R.id.epubReaderFragment,
                bundle
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}