package com.nastya.booktracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.nastya.booktracker.databinding.FragmentAddBooksBinding

class AddBooksFragment : Fragment() {
    private var _binding: FragmentAddBooksBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AddBookViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddBooksBinding.inflate(inflater, container, false)
        val view = binding.root

        val application = requireNotNull(this.activity).application
        val dao = BookDatabase.getInstance(application).bookDao
        val viewModelFactory = AddBookViewModelFactory(dao)
        val viewModel = ViewModelProvider(this, viewModelFactory)
            .get(AddBookViewModel::class.java)
        this.viewModel = viewModel

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bookPageAdd.addTextChangedListener { str ->
            str.toString().toIntOrNull()?.let { bookPage ->
                viewModel.onNewAllPagesCountChanged(bookPage)
            }
        }

        binding.bookNameAdd.addTextChangedListener { str ->
            viewModel.onBookNameChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        binding.bookAuthorAdd.addTextChangedListener { str ->
            viewModel.onBookAuthorChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        binding.bookDescAdd.addTextChangedListener { str ->
            viewModel.onBookDescChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        binding.bookImgAdd.addTextChangedListener { str ->
            viewModel.onBookImgChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        binding.btnAction.setOnClickListener {
            val name = binding.bookNameAdd.text.toString().trim()
            val author = binding.bookAuthorAdd.text.toString().trim()

            if (name.isEmpty() && author.isEmpty()) {
                binding.bookNameAdd.error = "Введите название"
                binding.bookNameAdd.requestFocus()

                binding.bookAuthorAdd.error = "Введите автора"
                binding.bookAuthorAdd.requestFocus()
            } else {
                viewModel.addTask()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}