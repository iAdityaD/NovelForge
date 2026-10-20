package com.novelforge.domain.workspace

import com.novelforge.core.model.*

val bookStatuses = listOf("In progress", "Completed", "On hold")

data class WorkspaceTemplate(val name: String, val description: String)

val workspaceTemplates =
    listOf(
        WorkspaceTemplate("01_Chapters", "Acts, chapters and scenes"),
        WorkspaceTemplate("02_Characters", "Profiles, backstories and relationships"),
        WorkspaceTemplate("03_Lore", "Worlds, magic, geography and history"),
        WorkspaceTemplate("04_Plot", "Outlines, timelines and subplots"),
        WorkspaceTemplate("05_Metadata", "Premise, genre, themes and references"),
    )

fun defaultFolder(kind: String) =
    when (kind) {
        "Chapter",
        "Screenplay" -> "01_Chapters"
        "Character",
        "Backstory" -> "02_Characters"
        "Location",
        "Worldbuilding",
        "Research" -> "03_Lore"
        "Arc",
        "Plot thread",
        "Timeline event" -> "04_Plot"
        else -> "05_Metadata"
    }

fun descendantIds(id: Long, entries: List<Entry>): Set<Long> {
    val children = entries.groupBy { it.folderId }
    val found = mutableSetOf<Long>()
    val queue = ArrayDeque<Long>()
    queue.add(id)
    while (queue.isNotEmpty()) {
        val next = queue.removeFirst()
        if (found.add(next)) children[next].orEmpty().forEach { queue.add(it.id) }
    }
    return found
}

fun canRelocate(entry: Entry, destination: Long?, entries: List<Entry>): Boolean {
    if (destination == null) return true
    val folder = entries.find { it.id == destination } ?: return false
    return folder.kind == "Folder" &&
        folder.bookId == entry.bookId &&
        !folder.deleted &&
        destination !in descendantIds(entry.id, entries)
}
