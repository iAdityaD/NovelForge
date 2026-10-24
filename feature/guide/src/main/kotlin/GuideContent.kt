package com.novelforge.app.ui

/** Help describes shipped behavior, separately from screen composition. */
internal data class GuideArticle(val id: String, val category: String, val title: String, val sections: List<Pair<String, String>>)

internal val guideArticles = listOf(
    GuideArticle("start", "Getting started", "Start a writing project", listOf(
        "Choose your starting point" to "Create a workspace from Library. Standard structure creates your selected starter folders and guides. From scratch starts empty. A title is enough to begin; planning is optional.",
        "Make it yours" to "Edit the title, author, multiple genres and status using the pencil beside the workspace menu. Edit the synopsis separately on Overview. In progress, On hold and Completed describe the whole project.",
        "Write immediately" to "Open Write and add a document. Use Document for a blank page, Chapter for a chapter template, or Screenplay for basic scene headings and dialogue. Screenplay is a writing template, not professional screenplay pagination.",
        "Change the defaults" to "Settings → Writing → Workspace structure changes folders for future standard projects. Existing projects and scratch projects keep their own structure."
    )),
    GuideArticle("views", "Getting started", "Find your way around", listOf(
        "Overview" to "Your synopsis, writing goal and shortcut back to the latest draft. The synopsis has its own edit button.",
        "Plan and Write" to "Plan holds resources such as people, places and research. Write holds manuscript drafts. A resource link does not make that resource part of the manuscript.",
        "Arrange and Read" to "Arrange selects and orders existing drafts. Read follows that order. Files is available across the workspace to organize the underlying folders and documents.",
        "Library and Analytics" to "Library search and filters find projects by title, status and genre. Analytics starts with all projects; choose one project to inspect its writing activity and manuscript totals. Page counts estimate 250 words per page."
    )),
    GuideArticle("resources", "Planning", "Create resources and research", listOf(
        "Add a resource" to "In Plan, tap +. Name the resource, select an existing category or create a new one. A scratch project has no preset categories. Canceling does not create a file or folder.",
        "Keep planning light" to "For a character, capture what they want and what stands in their way. For a place, record what changes the story. Add detail only when it helps you write.",
        "External sources" to "Add a website URL when creating an external source and keep your own notes alongside it. The app stores the link and notes; it does not download the article."
    )),
    GuideArticle("links", "Planning", "Link documents and resources", listOf(
        "Insert a link" to "Place the cursor in your draft, tap the editor’s link button and choose an existing resource or document. The picker inserts a stable reference. Use the picker instead of typing an invented numeric ID.",
        "Rename and move safely" to "References use document IDs, so moving or renaming the target preserves the connection. Existing written link labels remain as originally inserted.",
        "Follow connections" to "Tap a reference in reading mode to open its target. Plan’s connections button shows the graph. Story references in the editor menu shows incoming links. Restore a trashed target through Settings → Recently deleted."
    )),
    GuideArticle("draft", "Writing", "Draft without getting stuck", listOf(
        "Start with a change" to "Write one sentence about who wants something, what prevents it and what might happen next. Begin the scene at a moment of pressure. You can discover the outline while drafting.",
        "Keep moving" to "Use a short placeholder for a fact or transition you need to revisit. Store unused dialogue and scenes as resources or separate drafts. You do not need to finish profiles before writing.",
        "Revise in passes" to "Read first for story and order, then for scene purpose, then for sentence clarity. Save a checkpoint before substantial revisions. Up to 20 manual checkpoints are kept per document."
    )),
    GuideArticle("editor", "Writing", "Formatting and saving", listOf(
        "Markdown" to "Use ## Heading, **bold**, *italic*, > quote and - bullet. The formatting toolbar inserts syntax at the cursor or wraps the selection. Callouts use > [!NOTE]. Reading mode previews supported formatting.",
        "Your writing canvas" to "Focus mode hides the formatting toolbar. Settings → Editor controls writing typography and syntax colors. Reading typography has its own settings.",
        "Save state" to "Edits save locally after a short pause and before navigation. Saved locally means the database write succeeded. If saving fails, retry before leaving. A sudden process kill can still lose the latest unsaved fragment.",
        "Undo and checkpoints" to "Undo and redo belong to the current editing session. Checkpoints are persistent saved versions. Neither replaces an exported backup."
    )),
    GuideArticle("arrange", "Manuscript", "Assemble and read your manuscript", listOf(
        "Add existing drafts" to "In Arrange, tap Add document, select existing drafts and confirm. This does not create or copy chapters. New drafts remain outside the manuscript until you add them; standard starter chapters begin included.",
        "Set reading order" to "Move documents earlier or later using the arrow controls. Folder order in Files is independent of manuscript order. Read and manuscript export use the same included documents and order.",
        "Remove without deleting" to "Remove from manuscript excludes a draft from Read and export. Its text remains in Write and Files, ready to add again. Move to trash is a separate operation.",
        "Proofread" to "Open Read to begin or choose a document. Use the reader’s typography and fullscreen controls, bookmarks and navigation. Edit the source when you need to make a change."
    )),
    GuideArticle("safety", "Data and recovery", "Back up and restore your work", listOf(
        "Export a recovery backup" to "Use Back up workspace in the workspace menu, or Settings → Data & backup. Choose a destination outside the app using Android’s file picker. Make backups before uninstalling or replacing development builds.",
        "What is included" to "Current recovery backups include project metadata, documents, trash, revision checkpoints, manuscript order and writing activity. App settings, bookmarks, reading positions and unsaved drafts are not included. Older backups contain only the data supported by their format.",
        "Restore a copy" to "Use the import action beside search in Library, or Restore from backup in Settings. Restore creates a separate project and reconnects internal references without overwriting the original.",
        "Export versus backup" to "Export manuscript produces the arranged writing for use elsewhere. It is not a recovery backup. There is no automatic cloud sync; you can choose an available cloud destination in Android’s file picker."
    )),
)
