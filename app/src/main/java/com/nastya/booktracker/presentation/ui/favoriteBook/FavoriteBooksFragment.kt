package com.nastya.booktracker.presentation.ui.favoriteBook

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
import com.nastya.booktracker.data.local.database.BookDatabase
import com.nastya.booktracker.databinding.FragmentFavoriteBooksBinding
import com.nastya.booktracker.presentation.ui.bookList.BookItemAdapter
import kotlinx.coroutines.launch

class FavoriteBooksFragment : Fragment() {
    private var _binding: FragmentFavoriteBooksBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FavoriteBooksViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBooksBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViewModel()

        val adapter = BookItemAdapter(
            onItemClick = { bookId -> viewModel.onBookClicked(bookId) },
            onFavoriteClick = { bookId -> viewModel.toggleBookIsFavorite(bookId) }
        )
        binding.favBooksList.adapter = adapter

        observeFavoriteBooks(adapter)
        observeNavigation()
    }

    private fun initViewModel() {
        val application = requireNotNull(this.activity).application
        val dao = BookDatabase.getInstance(application).bookDao
        val viewModelFactory = FavoriteBooksViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[FavoriteBooksViewModel::class.java]
    }

    private fun observeFavoriteBooks(adapter: BookItemAdapter) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favoriteBooks.collect { books ->
                    books.let {
                        adapter.submitList(it)
                    }
                }
            }
        }
    }

    private fun observeNavigation() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateToBook.collect { bookId ->
                    bookId.let {
                        val action = FavoriteBooksFragmentDirections
                            .actionFavoriteBooksFragmentToBookDetailFragment(bookId)
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}