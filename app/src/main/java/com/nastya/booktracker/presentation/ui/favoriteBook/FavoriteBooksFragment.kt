package com.nastya.booktracker.presentation.ui.favoriteBook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.nastya.booktracker.data.local.database.BookDatabase
import com.nastya.booktracker.databinding.FragmentFavoriteBooksBinding
import com.nastya.booktracker.presentation.ui.bookList.BookItemAdapter

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
        val application = requireNotNull(this.activity).application
        val dao = BookDatabase.Companion.getInstance(application).bookDao
        val viewModelFactory = FavoriteBooksViewModelFactory(dao)
        viewModel = ViewModelProvider(
            this, viewModelFactory
        )[FavoriteBooksViewModel::class.java]

        val adapter = BookItemAdapter(
            onItemClick = { bookId ->
                viewModel.onBookClicked(bookId)
            },
            onFavoriteClick = { bookId ->
                viewModel.toggleBookIsFavorite(bookId)
            }
        )
        binding.favBooksList.adapter = adapter

        viewModel.filterByFavorite()

        viewModel.favoriteProducts.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        navigateToBookObserve()
    }

    private fun navigateToBookObserve() {
        viewModel.navigateToBook.observe(viewLifecycleOwner, Observer { bookId ->
            bookId?.let {
                val action = FavoriteBooksFragmentDirections.
                actionFavoriteBooksFragmentToBookDetailFragment(bookId)
                this.findNavController().navigate(action)
                viewModel.onBookNavigated()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}