package com.nastya.booktracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.nastya.booktracker.databinding.FragmentBooksBinding

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

        viewModel = ViewModelProvider(this).get(BooksViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = BookItemAdapter { bookId ->
            viewModel.onBookClicked(bookId)
        }
        binding.booksList.adapter = adapter

        viewModel.filteredProducts.observe(viewLifecycleOwner, Observer {
            it?.let { adapter.submitList(it) }
        })

        viewModel.navigateToBook.observe(viewLifecycleOwner, Observer { bookId ->
            bookId?.let {
                val action = BooksFragmentDirections.
                    actionBooksFragmentToEditBookFragment(bookId)
                this.findNavController().navigate(action)
                viewModel.onBookNavigated()
            }
        })

        viewModel.filterByCategory("all");
        binding.allBooksBtn.isSelected = true
        setupFilterButtons();

//        binding.

        return view
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}