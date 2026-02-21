package com.nastya.booktracker.presentation.ui.bookNotes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.nastya.booktracker.data.local.database.BookDatabase
import com.nastya.booktracker.databinding.FragmentBookNotesBinding
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

        initViewModel()

        val adapter = NoteItemAdapter()
        binding.notesList.adapter = adapter

        viewModel.loadNotesForBook(bookId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.highlights.collect { highlights ->
                adapter.submitList(highlights)
            }
        }
    }

    private fun initViewModel() {
        val application = requireNotNull(this.activity).application
        val highlightDao = BookDatabase.getInstance(application).highlightDao
        val viewModelFactory = BookNotesViewModelFactory(highlightDao)
        viewModel = ViewModelProvider(this, viewModelFactory)[BookNotesViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}