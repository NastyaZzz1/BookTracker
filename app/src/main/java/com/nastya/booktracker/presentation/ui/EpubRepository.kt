package com.nastya.booktracker.presentation.ui

import android.content.Context
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.asset.FileAsset
import org.readium.r2.shared.util.Try
import org.readium.r2.streamer.Streamer
import org.readium.r2.streamer.parser.PubBox
import java.io.File

class EpubRepository(
    private val context: Context
) {
    suspend fun extractMetadata(filePath: String): Publication {
        val streamer = Streamer(context.applicationContext)
        val asset = FileAsset(File(filePath))
        val result = streamer.open(asset, allowUserInteraction = false)

        return when (result) {
            is Try.Success -> when (val value = result.value) {
                is Publication -> value
                is PubBox -> value.publication
                else -> throw IllegalStateException("Invalid publication")
            }
            is Try.Failure -> throw IllegalStateException("Invalid publication")
        }
    }
}