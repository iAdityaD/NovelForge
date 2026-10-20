package com.novelforge.domain.workspace

import com.novelforge.core.model.wordCount
import java.util.Locale

object MetadataPolicy {
    fun genres(values: List<String>): List<String> {
        require(values.size <= 20) { "Choose at most 20 genres." }
        return values
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .onEach { require(it.length <= 80) { "Genre names must be 80 characters or fewer." } }
            .distinctBy { it.lowercase(Locale.ROOT) }
    }

    fun folder(name: String): String {
        val value = name.trim()
        require(value.isNotEmpty() && value.length <= 200) {
            "Use a folder name of 1–200 characters."
        }
        require(
            value !in listOf(".", "..") &&
                value.none { it == '/' || it == '\\' || it.isISOControl() }
        ) {
            "Folder names cannot include slashes or control characters."
        }
        return value
    }

    fun folders(names: Collection<String>): List<String> {
        val values = names.map(::folder)
        require(values.map { it.lowercase(Locale.ROOT) }.distinct().size == values.size) {
            "Folder names must be unique."
        }
        return values
    }

    fun acceptsSynopsis(previous: String, next: String, limit: Int): Boolean {
        val nextCount = wordCount(next)
        return nextCount <= limit ||
            (wordCount(previous) > limit && nextCount <= wordCount(previous))
    }

    fun synopsis(previous: String, next: String, limit: Int) {
        require(acceptsSynopsis(previous, next, limit)) {
            "Synopsis exceeds the $limit-word limit. Change the limit in Settings → Writing → Content limits."
        }
    }
}
