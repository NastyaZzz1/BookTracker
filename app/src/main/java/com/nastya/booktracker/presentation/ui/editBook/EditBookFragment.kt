package com.nastya.booktracker.presentation.ui.editBook

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.nastya.booktracker.data.local.database.BookDatabase
import com.nastya.booktracker.databinding.FragmentEditBookBinding
import kotlinx.coroutines.launch

class EditBookFragment : Fragment() {
    private var _binding: FragmentEditBookBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EditBookViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBookBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookId = EditBookFragmentArgs.fromBundle(requireArguments()).bookId
        val application = requireNotNull(this.activity).application
        val bookDao = BookDatabase.getInstance(application).bookDao
        val viewModelFactory = EditBookViewModelFactory(bookId, bookDao)
        viewModel = ViewModelProvider(this, viewModelFactory)[EditBookViewModel::class.java]

        observeBooks()
        observeSaveSuccess()
        observeFieldErrors()
        observeNavigation()
        setupTextWatchers()
        setupUpdateButton()
    }

    private fun observeBooks() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bookState.collect { book ->
                    book?.let {
                        if (binding.bookName.text.toString() != book.bookName)
                            binding.bookName.setText(book.bookName)

                        if (binding.bookAuthor.text.toString() != book.bookAuthor)
                            binding.bookAuthor.setText(book.bookAuthor)

                        if (binding.bookDesc.text.toString() != book.description)
                            binding.bookDesc.setText(book.description)
                    }
                }
            }
        }
    }

    private fun observeSaveSuccess() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveSuccess.collect {
                    Toast.makeText(context, "Изменения сохранены", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeFieldErrors() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fieldErrors.collect { errors ->
                    binding.bookName.error = errors?.nameError
                    binding.bookAuthor.error = errors?.authorError

                    when {
                        errors?.nameError != null -> binding.bookName.requestFocus()
                        errors?.authorError != null -> binding.bookAuthor.requestFocus()
                    }
                }
            }
        }
    }

    private fun observeNavigation() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateToDetail.collect {
                    view?.findNavController()?.popBackStack()
                }
            }
        }
    }

    private fun setupTextWatchers() {
        binding.bookName.addTextChangedListener { str ->
            viewModel.onBookNameChanged(str?.toString() ?: "")
        }

        binding.bookAuthor.addTextChangedListener { str ->
            viewModel.onBookAuthorChanged(str?.toString() ?: "")
        }

        binding.bookDesc.addTextChangedListener { str ->
            viewModel.onBookDescChanged(str?.toString() ?: "")
        }
    }

    private fun setupUpdateButton () {
        binding.updateButton.setOnClickListener {
            viewModel.updateTask()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}