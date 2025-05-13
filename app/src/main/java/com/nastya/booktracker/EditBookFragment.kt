package com.nastya.booktracker

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.nastya.booktracker.databinding.FragmentEditBookBinding

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

        val bookId = EditBookFragmentArgs.fromBundle(requireArguments()).bookId

        val application = requireNotNull(this.activity).application
        val bookDao = BookDatabase.getInstance(application).bookDao
        val dailyReadingDao = BookDatabase.getInstance(application).dailyReadingDao

        val viewModelFactory = EditBookViewModelFactory(bookId, bookDao, dailyReadingDao)
        val viewModel = ViewModelProvider(this, viewModelFactory)[EditBookViewModel::class.java]

        this.viewModel = viewModel

        viewModel.navigateToList.observe(viewLifecycleOwner, Observer { navigate->
            if(navigate) {
                view.findNavController()
                    .navigate(R.id.action_editBookFragment_to_booksFragment)
                viewModel.onNavigatedToList()
            }
        })

        viewModel.book.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.bookName.setText(it.bookName)
                binding.bookAuthor.setText(it.bookAuthor)
                binding.bookDesc.setText(it.description)
                binding.bookImg.setText(it.imageUrl)
                binding.bookReadPages.setText(it.readPagesCount.toString())
                binding.bookAllPages.setText(it.allPagesCount.toString())
            }
        })

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.bookName.addTextChangedListener { str ->
            viewModel.onBookNameChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        binding.bookAuthor.addTextChangedListener { str ->
            viewModel.onBookAuthorChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        binding.bookDesc.addTextChangedListener { str ->
            viewModel.onBookDescChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        binding.bookImg.addTextChangedListener { str ->
            viewModel.onBookImgChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        binding.bookAllPages.addTextChangedListener { str ->
            str.toString().toIntOrNull()?.let { bookPage ->
                viewModel.onAllPagesCountChanged(bookPage)
            }
        }

        binding.bookReadPages.addTextChangedListener { str ->
            str.toString().toIntOrNull()?.let { bookPage ->
                viewModel.onReadPagesCountChanged(bookPage)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                binding.bookReadPages.error = it
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        binding.updateButton.setOnClickListener {
            if (viewModel.errorMessage.value == null) viewModel.updateTask()
            else Toast.makeText(context, "Исправьте ошибки перед сохранением", Toast.LENGTH_SHORT).show()
        }

        binding.deleteButton.setOnClickListener {
            viewModel.deleteTask()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}