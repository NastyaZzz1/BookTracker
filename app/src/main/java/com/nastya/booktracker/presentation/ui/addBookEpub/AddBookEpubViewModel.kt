package com.nastya.booktracker.presentation.ui.addBookEpub

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nastya.booktracker.data.local.dao.BookDao
import com.nastya.booktracker.domain.model.Book
import com.nastya.booktracker.presentation.ui.BookFileManager
import com.nastya.booktracker.presentation.ui.EpubRepository
import kotlinx.coroutines.launch
import java.io.File
import java.util.zip.ZipFile
import com.nastya.booktracker.R
import java.io.ByteArrayOutputStream
import androidx.core.graphics.createBitmap
import org.readium.r2.shared.publication.Link

class AddBookEpubViewModel(private val dao: BookDao): ViewModel() {

    fun addBookFromUri(
        context: Context,
        bookUri: Uri,
        epubRepository: EpubRepository
    ) {
        viewModelScope.launch {
            try {
                when (val saveResult = BookFileManager.saveBookFromUri(context, bookUri)) {
                    is BookFileManager.SaveBookResult.Success -> {
                        val publication = epubRepository.extractMetadata(saveResult.filePath)

                        val galleryImageBytes = drawableToByteArray(context, R.drawable.icon_default_book_cover)
                        val coverBytes = getCoverBytes(File(saveResult.filePath), galleryImageBytes)

                        val book = Book(
                            bookName = publication.metadata.title,
                            bookAuthor = publication.metadata.authors.firstOrNull()?.name ?: "bookAuthor",
                            description = publication.metadata.description ?: "bookDescription",
                            imageData = coverBytes,
                            allPagesCount = 1,
                            filePath = saveResult.filePath,
                            chaptersCount = countChapters(publication.tableOfContents),
                            fileNameFromUri = BookFileManager.getFileNameFromUri(context, bookUri)
                        )
                        dao.insert(book)

                        Toast.makeText(context, "Книга добавлена", Toast.LENGTH_SHORT).show()
                    }
                    is BookFileManager.SaveBookResult.Error -> { }
                }
            } catch(e: Exception) {
                Log.e("BookViewModel", "Ошибка добавления книги", e)
            }
        }
    }

    fun countChapters(links: List<Link>): Int {
        return links.sumOf { link ->
            if (link.children.isEmpty()) {
                1
            } else {
                countChapters(link.children)
            }
        }
    }

    fun getCoverBytes(
        epubFile: File,
        fallbackBytes: ByteArray
    ): ByteArray {
        return try {
            ZipFile(epubFile).use { zip ->
                val coverEntry = zip.entries().asSequence()
                    .firstOrNull { entry ->
                        entry.name.contains("cover", ignoreCase = true) &&
                        (entry.name.endsWith(".jpg", true) ||
                            entry.name.endsWith(".jpeg", true) ||
                            entry.name.endsWith(".png", true)
                        )
                    }

                coverEntry?.let {
                    zip.getInputStream(it).use { input ->
                        input.readBytes()
                    }
                } ?: fallbackBytes
            }
        } catch (e: Exception) {
            fallbackBytes
        }
    }


    fun drawableToByteArray(context: Context, drawableResId: Int): ByteArray {
        val drawable = ContextCompat.getDrawable(context, drawableResId) ?: return ByteArray(0)

        val bitmap = if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val bmp = createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(1),
                drawable.intrinsicHeight.coerceAtLeast(1)
            )
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bmp
        }

        return ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
    }
}