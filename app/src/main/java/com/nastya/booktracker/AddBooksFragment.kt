package com.nastya.booktracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.nastya.booktracker.databinding.FragmentAddBooksBinding

class AddBooksFragment : Fragment() {
    private var _binding: FragmentAddBooksBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddBooksBinding.inflate(inflater, container, false)
        val view = binding.root

        val application = requireNotNull(this.activity).application
        val dao = BookDatabase.getInstance(application).bookDao
        val viewModelFactory = AddBookViewModelFactory(dao)
        val viewModel = ViewModelProvider(
            this, viewModelFactory).get(AddBookViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}