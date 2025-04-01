package com.nastya.booktracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.nastya.booktracker.databinding.FragmentEditBookBinding

class EditBookFragment : Fragment() {
    private var _binding: FragmentEditBookBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditBookBinding.inflate(inflater, container, false)
        val view = binding.root

        val bookId = EditBookFragmentArgs.fromBundle(requireArguments()).bookId

        val viewModelFactory = EditBookViewModelFactory(bookId)
        val viewModel = ViewModelProvider(this, viewModelFactory)
                            .get(EditBookViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.navigateToList.observe(viewLifecycleOwner, Observer { navigate->
            if(navigate) {
                view.findNavController()
                    .navigate(R.id.action_editBookFragment_to_booksFragment)
                viewModel.onNavigatedToList()
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}