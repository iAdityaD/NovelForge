package com.novelforge.core.model

/** Stable template keys keep a folder's purpose when its display name changes. */
data class StarterFolder(val key: String, val name: String, val enabled: Boolean = true)

val defaultStarterFolders = listOf(
    StarterFolder("overview", "Novel Overview"),
    StarterFolder("ideas", "Story Ideas"),
    StarterFolder("characters", "Characters"),
    StarterFolder("world", "Story World"),
    StarterFolder("plot", "Plot & Timeline"),
    StarterFolder("manuscript", "Manuscript"),
    StarterFolder("scenes", "Scene Ideas"),
    StarterFolder("archive", "Archive"),
)
