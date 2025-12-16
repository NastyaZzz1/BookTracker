package com.nastya.booktracker.presentation.ui.bookDetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.nastya.booktracker.R
import com.nastya.booktracker.data.local.database.BookDatabase
import com.nastya.booktracker.databinding.FragmentBookDetailBinding
import kotlinx.coroutines.launch

class BookDetailFragment : Fragment() {
    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BookDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookId = BookDetailFragmentArgs.fromBundle(requireArguments()).bookId

        val application = requireNotNull(this.activity).application
        val dao = BookDatabase.Companion.getInstance(application).bookDao
        val viewModelFactory = BookDetailViewModelFactory(bookId, dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[BookDetailViewModel::class.java]

        bookItemObserver()
        setupFavButton()
        setupChangeButton(bookId)
        setupNotesButton()
        setupDeleteButton()
        setupReadButton(bookId)

        lifecycleScope.launch {
            viewModel.navigateToList.collect {
                it.let {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun setupFavButton() {
        binding.favBtn.setOnClickListener{
            viewModel.isFavoriteChanged()
        }
    }

    private fun setupDeleteButton() {
        binding.deleteButton.setOnClickListener {
            viewModel.showDeleteConfirmationDialog(requireContext())
        }
    }

    private fun setupNotesButton() {
        binding.notesBtn.setOnClickListener {
            findNavController().navigate(
                R.id.action_bookDetailFragment_to_bookNotesFragment
            )
        }
    }

    private fun setupReadButton(bookId: Long) {
        binding.readingBtn.setOnClickListener {
            lifecycleScope.launch {
                viewModel.book.let {
                    val action = BookDetailFragmentDirections
                        .actionBookDetailFragmentToEpubReaderFragment(
                            bookPath = it.value!!.filePath
                        )
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun setupChangeButton(bookId: Long) {
        binding.changeBtn.setOnClickListener {
            val action = BookDetailFragmentDirections.
            actionBookDetailFragmentToEditBookFragment(bookId)
            this.findNavController().navigate(action)
        }
    }

    private fun bookItemObserver() {
        viewModel.book.observe(viewLifecycleOwner, Observer { book ->
            book?.let {
                val allPagesCount = if(book.allPagesCount == 0) 1 else book.allPagesCount
                val progress = (book.readPagesCount * 100 ) / allPagesCount
                binding.linProgressBar.progress = progress
                binding.linProgressText.text = "$progress%"
                binding.bookName.text = book.bookName
                binding.bookAuthor.text = book.bookAuthor
                binding.bookDesc.text = book.description
                binding.bookReadPages.text = "Прочитано: ${book.readPagesCount}"
                binding.bookAllPages.text = "Всего страниц: ${book.allPagesCount}"
                binding.bookImg.load(book.imageData) {
                    crossfade(true)
                }
                binding.favBtn.setImageResource(
                    if (book.isFavorite) R.drawable.icon_heart
                    else R.drawable.icon_heart_empty
                )
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}