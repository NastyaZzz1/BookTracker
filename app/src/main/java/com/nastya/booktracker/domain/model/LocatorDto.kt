package com.nastya.booktracker.domain.model

import org.readium.r2.shared.publication.Locator

data class LocatorDto(
    val href: String,
    val type: String?,
    val progression: Double?,
    val totalProgression: Double?,
    val position: Int?,
    val text: TextDto?
) {
    data class TextDto(
        val highlight: String
    )

    fun toLocator(): Locator {
        return Locator(
            href = href,
            type = type ?: "application/xhtml+xml",
            locations = Locator.Locations(
                progression = progression,
                totalProgression = totalProgression,
                position = position,
            ),
            text = text?.let {
                Locator.Text(highlight = it.highlight)
            } ?: Locator.Text()
        )
    }

    companion object {
        fun fromLocator(locator: Locator): LocatorDto {
            return LocatorDto(
                href = locator.href,
                type = locator.type,
                progression = locator.locations.progression,
                totalProgression = locator.locations.totalProgression,
                position = locator.locations.position,
                text = locator.text.let {
                    it.highlight?.let { highlight -> TextDto(highlight = highlight) }
                }
            )
        }
    }
}
