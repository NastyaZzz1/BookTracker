package com.nastya.booktracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.nastya.booktracker.databinding.FragmentAddBooksBinding
import kotlinx.coroutines.launch

class AddBooksFragment : Fragment() {
    private var _binding: FragmentAddBooksBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AddBookViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBooksBinding.inflate(inflater, container, false)
        val view = binding.root

        val application = requireNotNull(this.activity).application
        val dao = BookDatabase.getInstance(application).bookDao
        val viewModelFactory = AddBookViewModelFactory(dao)
        val viewModel = ViewModelProvider(this, viewModelFactory)
            .get(AddBookViewModel::class.java)
        this.viewModel = viewModel

        viewModel.navigateToBack.observe(viewLifecycleOwner, Observer { navigate ->
            if(navigate) {
                view.findNavController().popBackStack()
            }
        })

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

            viewModel.viewModelScope.launch {
                if (name.isEmpty() && author.isEmpty()) {
                    binding.bookNameAdd.error = "Введите название"
                    binding.bookNameAdd.requestFocus()

                    binding.bookAuthorAdd.error = "Введите автора"
                    binding.bookAuthorAdd.requestFocus()
                } else if (!viewModel.isAvailableBook(name, author)) {
                    Toast.makeText(context, "Такая книга уже существует", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addTask()
                    Toast.makeText(context, "Книга добавлена", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}