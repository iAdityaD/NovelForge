package com.novelforge.domain.workspace

import com.novelforge.core.model.*

/** Creation recipes only. Instantiated documents belong to the project, not its recipe. */
data class ProjectRecipe(
    val id: String,
    val name: String,
    val folders: List<StarterFolder>,
    val draftKind: String = "Document",
    val draftTitles: List<String> = emptyList(),
)

val projectRecipes = listOf(
    ProjectRecipe("novel", "Novel", defaultStarterFolders, "Chapter", listOf("Chapter 1", "Chapter 2")),
    ProjectRecipe("short-story", "Short story", listOf(StarterFolder("overview", "Story Overview"), StarterFolder("ideas", "Ideas"), StarterFolder("manuscript", "Draft")), draftTitles = listOf("First draft")),
    ProjectRecipe("screenplay", "Screenplay", listOf(StarterFolder("overview", "Project Overview"), StarterFolder("characters", "Characters"), StarterFolder("plot", "Outline"), StarterFolder("manuscript", "Scenes")), "Screenplay", listOf("Scene 1")),
    ProjectRecipe("nonfiction", "Nonfiction", listOf(StarterFolder("overview", "Project Overview"), StarterFolder("research", "Research"), StarterFolder("plot", "Outline"), StarterFolder("manuscript", "Sections")), draftTitles = listOf("Introduction")),
    ProjectRecipe("essay", "Essay", listOf(StarterFolder("overview", "Project Overview"), StarterFolder("research", "Sources"), StarterFolder("manuscript", "Draft")), draftTitles = listOf("First draft")),
)

fun projectRecipe(id: String, novelFolders: List<StarterFolder>): ProjectRecipe {
    val recipe = projectRecipes.firstOrNull { it.id == id } ?: throw IllegalArgumentException("Choose an available project template.")
    return if (id == "novel") recipe.copy(folders = validateStarterFolders(novelFolders)) else recipe
}
