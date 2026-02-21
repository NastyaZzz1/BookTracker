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
import androidx.lifecycle.Observer
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

        binding.bookName.addTextChangedListener { str ->
            viewModel.onBookNameChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        binding.bookAuthor.addTextChangedListener { str ->
            viewModel.onBookAuthorChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        binding.bookDesc.addTextChangedListener { str ->
            viewModel.onBookDescChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        updateBtnListener()
        bookObserve()
        navigateToDetailObserve()
    }

    private fun bookObserve() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bookState.collect { book ->
                    book?.let {
                        binding.bookName.setText(it.bookName)
                        binding.bookAuthor.setText(it.bookAuthor)
                        binding.bookDesc.setText(it.description)
                    }
                }
            }
        }
    }

    private fun navigateToDetailObserve() {
        viewModel.navigateToDetail.observe(viewLifecycleOwner, Observer { navigate ->
            if (navigate) {
                view?.findNavController()?.popBackStack()
                viewModel.onNavigatedToList()
            }
        })
    }

    private fun updateBtnListener () {
        binding.updateButton.setOnClickListener {
            if (viewModel.errorMessage.value == null) {
                viewModel.updateTask()
                Toast.makeText(context, "Изменения сохранены", Toast.LENGTH_SHORT).show()
            }
            else Toast.makeText(context, "Исправьте ошибки перед сохранением", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}