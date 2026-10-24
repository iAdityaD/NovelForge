package com.novelforge.domain.workspace

import com.novelforge.core.model.*

data class CreatedWorkspace(val bookId: String, val overviewId: Long?)

fun validateStarterFolders(folders: List<StarterFolder>): List<StarterFolder> {
    require(folders.size <= 40) { "Use at most 40 starter folders." }
    require(folders.map { it.key }.distinct().size == folders.size) { "Folder keys must be unique." }
    require(folders.all { it.key.isNotBlank() }) { "A folder key is required." }
    MetadataPolicy.folders(folders.map { it.name })
    return folders.map { it.copy(name = MetadataPolicy.folder(it.name)) }
}

fun folderGuide(folder: StarterFolder, book: Book): String {
    val purpose: String
    val contents: String
    val action: String
    when (folder.key) {
        "overview" -> {
            purpose = "Your project’s central reference. Keep the big picture and your next step close at hand."
            contents = "Working title, genres, premise, synopsis, main characters, writing status, important decisions and next steps."
            action = "Write one sentence about the story you want to tell."
        }
        "ideas" -> {
            purpose = "A place for your story to take shape before every detail is clear."
            contents = "Initial concepts, premises, synopses, themes, possible endings and developing ideas."
            action = "Capture one idea, question or possible ending."
        }
        "characters" -> {
            purpose = "Get to know the people who drive your story."
            contents = "Individual profiles with personality, background, goals, motivations, relationships and character development."
            action = "Create a Character document for someone who wants something."
        }
        "world" -> {
            purpose = "Build the places and rules that make your story feel real."
            contents = "Settings, locations, history, culture, institutions and rules that affect the story."
            action = "Describe one place your characters will visit."
        }
        "plot" -> {
            purpose = "See how events connect and how the story changes from beginning to end."
            contents = "Major events, outlines, chronology, turning points, revelations and parallel storylines."
            action = "Note a possible beginning, turning point or ending."
        }
        "manuscript" -> {
            purpose = "The manuscript itself. Begin drafting here whenever you are ready."
            contents = "Chapters, optional parts and the scenes that belong in your current manuscript."
            action = "Open a draft in Write and begin your first line."
        }
        "scenes" -> {
            purpose = "Save moments that do not yet have a place in the manuscript."
            contents = "Unplaced scenes, dialogue, memorable moments, twists and fragments worth keeping."
            action = "Write a few lines of dialogue or a moment you can picture."
        }
        "archive" -> {
            purpose = "Keep earlier material without crowding your active workspace."
            contents = "Previous drafts, deleted scenes, abandoned ideas and alternatives you may return to."
            action = "Move an older document here when you want to keep it for reference."
        }
        else -> {
            purpose = "A flexible space for the material you want to collect."
            contents = "Notes, references and documents that fit this folder’s purpose."
            action = "Create one note and give it a useful name."
        }
    }
    val profile = if (folder.key == "overview") """

## Story decisions
- Core premise:
- Main characters:
- Important decisions:
- Next steps:

Edit the title, genres and status using the workspace pencil. Edit the synopsis on Overview; those fields are the project's central reference.

""" else ""
    return """# ${folder.name}

$purpose

## What belongs here
$contents

## How to use this folder
Create a document for each idea or subject. Use [[links]] to connect related notes through the editor’s link picker. Rename, move or remove documents and folders as your novel grows. This README is editable too.

All planning is optional. You can begin drafting immediately, leave template fields blank, and return to planning later.

## Start here
$action
$profile"""
}
