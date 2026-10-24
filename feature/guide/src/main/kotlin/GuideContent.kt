package com.novelforge.app.ui

/** Help describes shipped behavior, separately from screen composition. */
internal data class GuideArticle(val id: String, val category: String, val title: String, val sections: List<Pair<String, String>>)

internal val guideArticles = listOf(
    GuideArticle("start", "Getting started", "Start a writing project", listOf(
        "Choose your starting point" to "Create a workspace from Library. Standard structure offers optional Novel, Short story, Screenplay, Nonfiction and Essay templates. Build from scratch starts empty. Only a title is required.",
        "Make it yours" to "Optional details include author, multiple genres and color. Use the workspace pencil to change these and the project status. Edit the synopsis separately on Overview. A word goal of zero disables the goal.",
        "Keep the structure flexible" to "Templates create ordinary folders and files. Rename, move, reorder or delete them as your work evolves. Settings → Writing → Workspace structure customizes the Novel starter folders for future projects. Existing projects stay unchanged."
    )),
    GuideArticle("views", "Getting started", "Plan, Write and Arrange", listOf(
        "Plan" to "Collect research, notes, sources, ideas and anything else that supports the writing. Create your own categories as folders, including nested folders. Type labels are optional and can be named freely.",
        "Write" to "Create the manuscript as nested folders and documents. Folders can represent parts, acts, chapters or your own grouping. Documents hold the actual writing. Names and type labels do not determine whether something is manuscript content.",
        "Arrange" to "Organize the same manuscript tree shown in Write. Every manuscript document belongs here automatically. There is no separate add-to-manuscript list and no copy of your text.",
        "Files and search" to "Files gives access to the full workspace. Search locates file names; Filter files narrows the view by the types and statuses already in your project. Read mode is deferred for now."
    )),
    GuideArticle("resources", "Planning", "Create resources and research", listOf(
        "Capture an idea" to "In Plan, tap + and choose New resource. Add a title and optionally a type label, such as Idea, Interview or Research. Select a folder, create a category in the form, or save at the top level.",
        "Organize naturally" to "Use + → New folder to create categories and subfolders. Select a folder before adding items to save them there. Rename and move items through their actions menu. There is no required character or world-building taxonomy.",
        "External sources" to "Add an optional http or https source URL. The document stores the address and an area for notes. You can write article notes or quotations yourself; the app does not download the article."
    )),
    GuideArticle("links", "Planning", "Link documents and resources", listOf(
        "Insert a link" to "Place the cursor in your manuscript, tap the editor’s link button and select a resource or document. The picker inserts a stable reference. Use the picker instead of typing an invented numeric ID.",
        "Keep references intact" to "Links use document IDs. Moving or renaming the target preserves the connection; existing written link labels remain as originally inserted. Linking a resource does not add it to the manuscript.",
        "Find references" to "Story references in the editor menu shows incoming links. Restore trashed targets through Settings → Recently deleted."
    )),
    GuideArticle("draft", "Writing", "Draft without getting stuck", listOf(
        "Start with a change" to "Write one sentence about who wants something, what prevents it and what might happen next. Begin the scene at a moment of pressure. Planning is optional.",
        "Keep moving" to "Use a short placeholder for a fact or transition you need to revisit. Keep unused dialogue, ideas and research in Plan. You do not need complete profiles before writing.",
        "Revise in passes" to "Check story order first, then scene purpose, then sentence clarity. Save a checkpoint before substantial revisions. Up to 20 manual checkpoints are kept per document."
    )),
    GuideArticle("editor", "Writing", "Formatting and saving", listOf(
        "Markdown" to "Use ## Heading, **bold**, *italic*, > quote and - bullet. The toolbar inserts syntax at the cursor or wraps selected text. Callouts use > [!NOTE]. The editor retains Markdown source.",
        "Your writing canvas" to "Focus mode hides extra controls. Settings → Editor controls writing text size, monospace text and syntax colors. The writing canvas scrolls as your text grows.",
        "Save state" to "Edits save locally after a short pause and before navigation. Saved locally means the database write succeeded. If saving fails, retry before leaving. A sudden process kill can still lose the latest unsaved fragment.",
        "Undo and checkpoints" to "Undo and redo belong to the editing session. Checkpoints are persistent saved versions. Neither replaces an exported backup."
    )),
    GuideArticle("arrange", "Manuscript", "Arrange a nested manuscript", listOf(
        "Drag into order" to "Open Arrange. Long-press a row by its drag handle, then drag over another row. Drop in the upper half to place it before that item, or the lower half to place it after. Holding near the list edge scrolls the outline.",
        "Move between folders" to "Dropping beside an item adopts that item's parent folder. Use the actions menu → Move to choose a folder directly. Move up and Move down are alternatives to dragging. Folder moves cannot create loops.",
        "One manuscript" to "Write, Arrange, manuscript counts and export use the same nested structure. All manuscript documents are included. Planning resources stay separate, even when referenced from the text.",
        "Delete safely" to "Trash moves a file or a folder and its contents to Recently deleted. Restore it in Settings. Deleting an entire workspace is permanent and requires confirmation."
    )),
    GuideArticle("safety", "Data and recovery", "Back up and restore your work", listOf(
        "Export a recovery backup" to "Use Back up workspace in the workspace menu, or Settings → Data & backup. Choose a destination outside the app with Android’s file picker. Back up before uninstalling or replacing development builds.",
        "What is included" to "Current backups preserve project metadata, folders, documents, planning/manuscript roles, custom type labels, trash, revision checkpoints and writing activity. App settings, bookmarks, old reader positions and unsaved drafts are not included. Older backups contain only the data supported by their format.",
        "Restore a copy" to "Use Import backup beside search in Library, or Restore from backup in Settings. Restore creates a separate project and reconnects internal links without overwriting the original.",
        "Export versus backup" to "Export manuscript produces the writing in nested manuscript order. It is not a recovery backup. There is no automatic cloud sync; Android’s picker can save to an available cloud destination."
    )),
)
