package com.nastya.booktracker.presentation.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object BookFileManager {
    suspend fun saveBookFromUri(
        context: Context,
        bookUri: Uri,
        originalName: String? = null
    ): SaveBookResult {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = originalName ?: getFileNameFromUri(context, bookUri)

                Log.d("BookFileManager", "Сохранение книги: $fileName")

                val booksDir = File(context.filesDir, "books")
                if (!booksDir.exists()) {
                    booksDir.mkdirs()
                }

                val destinationFile = File(booksDir, fileName)
                if (destinationFile.exists()) {
                    Toast.makeText(context, "Такая книга уже добавлена", Toast.LENGTH_SHORT).show()
                    return@withContext SaveBookResult.Error(
                        "Файл с именем '$fileName' уже существует"
                    )
                }

                var bytesCopied = 0L
                context.contentResolver.openInputStream(bookUri)?.use { inputStream ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        bytesCopied = inputStream.copyTo(outputStream)
                    }
                }
                if (bytesCopied == 0L) {
                    return@withContext SaveBookResult.Error("Не удалось скопировать файл")
                }

                SaveBookResult.Success(
                    filePath = destinationFile.absolutePath,
                    fileName = fileName
                )

            } catch (e: Exception) {
                Log.e("BookFileManager", "Ошибка сохранения книги", e)
                SaveBookResult.Error("Ошибка: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }

    suspend fun deleteBook(
        context: Context,
        fileName: String
    ) {
        return withContext(Dispatchers.IO) {
            try {
                val booksDir = File(context.filesDir, "books")
                val file = File(booksDir, fileName)

                if (file.exists()) {
                    Log.d("BookFileManager", "Файл '$fileName' удален")
                    file.delete()
                } else {
                    Log.d("BookFileManager", "Файл '$fileName' не найден")
                }
            } catch (e: Exception) {
                Log.e("BookFileManager", "Ошибка при удалении файла: $fileName", e)
            }
        }
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var fileName: String? = null
        try {
            context.contentResolver.query(
                uri,
                null,
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BookFileManager", "Ошибка получения имени файла", e)
        }
        return fileName ?: "book_${System.currentTimeMillis()}.epub"
    }

    sealed class SaveBookResult {
        data class Success(
            val filePath: String,
            val fileName: String
        ) : SaveBookResult()

        data class Error(val message: String) : SaveBookResult()
    }
}