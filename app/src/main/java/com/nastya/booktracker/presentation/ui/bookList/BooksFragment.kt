package com.nastya.booktracker.presentation.ui.bookList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.nastya.booktracker.R
import com.nastya.booktracker.data.local.database.BookDatabase
import com.nastya.booktracker.databinding.FragmentBooksBinding
import kotlinx.coroutines.launch

class BooksFragment : Fragment() {
    private var _binding: FragmentBooksBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BooksViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBooksBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val application = requireNotNull(this.activity).application
        val dao = BookDatabase.getInstance(application).bookDao
        val viewModelFactory = BookViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[BooksViewModel::class.java]

        val adapter = BookItemAdapter(
            onItemClick = { bookId ->
                viewModel.onBookClicked(bookId)
            },
            onFavoriteClick = { bookId ->
                viewModel.toggleBookIsFavorite(bookId)
            }
        )
        binding.booksList.adapter = adapter

        lifecycleScope.launch {
            viewModel.filteredBooks.collect { books ->
                books.let {
                    adapter.submitList(books)
                }
            }
        }

        viewModel.updateAllCategories()
        viewModel.filterByCategory("all")
        binding.allBooksBtn.isSelected = true
        setSortIcon()
        setupFilterButtons()
        setupSortButton()
        navigateToBookObserver()
    }

    private fun setupSortButton() {
        binding.sortBtn.setOnClickListener {
            viewModel.changeSortedState()
        }
    }

    private fun navigateToBookObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateToDetail.collect { bookId ->
                    bookId?.let {
                        val action = BooksFragmentDirections
                            .actionBooksFragmentToBookDetailFragment(bookId)
                        findNavController().navigate(action)
                        viewModel.onBookNavigated()
                    }
                }
            }
        }
    }

    private fun setupFilterButtons() {
        binding.allBooksBtn.setOnClickListener { filterProducts("all") }
        binding.wantBooksBtn.setOnClickListener { filterProducts("want") }
        binding.readingBooksBtn.setOnClickListener { filterProducts("reading") }
        binding.pastBooksBtn.setOnClickListener { filterProducts("past") }
    }

    private fun filterProducts(category: String) {
        viewModel.filterByCategory(category)
        updateButtonStates(category)
    }

    private fun updateButtonStates(selectedCategory: String) {
        binding.allBooksBtn.isSelected = selectedCategory == "all"
        binding.wantBooksBtn.isSelected = selectedCategory == "want"
        binding.readingBooksBtn.isSelected = selectedCategory == "reading"
        binding.pastBooksBtn.isSelected = selectedCategory == "past"
    }

    private fun setSortIcon() {
        lifecycleScope.launch {
            viewModel.sortedBooksState.collect { state ->
                when (state) {
                    is BooksViewModel.SortedState.None -> binding.sortBtn.setImageResource(R.drawable.icon_sort_none)
                    is BooksViewModel.SortedState.Desc -> binding.sortBtn.setImageResource(R.drawable.icon_sort_desc)
                    is BooksViewModel.SortedState.Asc -> binding.sortBtn.setImageResource(R.drawable.icon_sort_asc)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}