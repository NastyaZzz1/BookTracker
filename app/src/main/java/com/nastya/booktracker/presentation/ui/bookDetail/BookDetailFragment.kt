package com.nastya.booktracker.presentation.ui.bookDetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.nastya.booktracker.R
import com.nastya.booktracker.data.local.database.BookDatabase
import com.nastya.booktracker.databinding.FragmentBookDetailBinding
import com.nastya.booktracker.domain.model.Book
import kotlinx.coroutines.launch

class BookDetailFragment : Fragment() {
    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BookDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                viewModel.bookState.let {
                    val action = BookDetailFragmentDirections
                        .actionBookDetailFragmentToEpubReaderFragment(
                            bookPath = it.value!!.filePath,
                            bookId = bookId
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bookState.collect { book ->
                    book?.let {
                        binding.favBtn.setImageResource(
                            if (it.isFavorite) R.drawable.icon_heart
                            else R.drawable.icon_heart_empty
                        )
                        updateUI(it)
                    }
                }
            }
        }
    }

    fun updateUI(book: Book) {
        val progress = book.progress
        binding.linProgressBar.progress = progress
        binding.linProgressText.text = "$progress%"
        binding.bookName.text = book.bookName
        binding.bookAuthor.text = book.bookAuthor
        binding.bookDesc.text = book.description
        binding.bookReadPages.text = "Глав: ${book.chaptersCount}"
        binding.bookAllPages.text = "Печатных страниц: ≈${book.allPagesCount}"

        if (binding.bookImg.tag != book.filePath) {
            binding.bookImg.load(book.imageData) {
                crossfade(true)
            }
            binding.bookImg.tag = book.filePath
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}