package com.novelforge.app.ui

/** Help describes shipped behavior, separately from screen composition. */
internal data class GuideArticle(val id: String, val category: String, val title: String, val sections: List<Pair<String, String>>)

internal val guideArticles = listOf(
    GuideArticle("start", "Getting started", "Start a writing project", listOf(
        "Choose your starting point" to "Create a workspace from Library. Standard structure offers optional Novel, Short story, Screenplay, Nonfiction and Essay templates. Build from scratch starts empty. Only a title is required. Save your own structures in Settings → Writing → My templates; optionally include empty documents. Saved templates also preserve custom field definitions.",
        "Make it yours" to "Optional details include author, multiple genres and color. Use the workspace pencil to change these and the project status. Edit the synopsis separately on Overview. A word goal of zero disables the goal.",
        "Keep the structure flexible" to "Templates create ordinary folders and files. Rename, move, reorder or delete them as your work evolves. Settings → Writing → Workspace structure customizes the Novel starter folders for future projects. Existing projects stay unchanged."
    )),
    GuideArticle("views", "Getting started", "Your workspace", listOf(
        "Overview" to "Edit the novel title, author, genres, status, synopsis and writing goal. Use the cover button to import an image; a compressed copy is stored locally and included in project backups.",
        "Write" to "Write resumes your last document. On your first visit, choose New file or Attach link. New file opens an untitled draft; Save asks for a filename and destination, with an optional new folder. The draft stays outside the file tree until saved. Folders can represent parts, acts, chapters or your own grouping. Documents hold the actual writing. Names and type labels do not determine whether something is manuscript content.",
        "Organize while writing" to "Write also arranges the manuscript. All workspace files are accessible here. Only manuscript-role documents are included in manuscript exports and writing totals. Open Files, then Outline options → Reorder files to show drag handles. There is no separate inclusion list or copy of your text.",
        "Files and search" to "Files gives access to the full workspace. Search locates file names; Filter files narrows the view by the types and statuses already in your project. The owl AI tab is reserved for a future writing companion and currently displays Coming soon. Use Read / Write at the top of a document to preview formatting or edit. Read shows your latest text without changing it."
    )),
    GuideArticle("resources", "Planning", "Create resources and research", listOf(
        "Capture an idea" to "In Write, choose New file and begin typing. Save it to your chosen folder. New files inherit the destination’s manuscript/reference role. Change that role from file actions when needed. Attach link stores a named website address and editable notes; it does not download the page.",
        "Organize naturally" to "Use the new-folder icon to create categories and subfolders. Select a folder before adding items to save them there. Rename and move items through their actions menu. There is no required character or world-building taxonomy.",
        "External sources" to "Add an optional http or https source URL. The document stores the address and an area for notes. You can write article notes or quotations yourself; the app does not download the article."
    )),
    GuideArticle("links", "Planning", "Link documents and resources", listOf(
        "Insert a link" to "Place the cursor in your manuscript, tap the editor’s link button and select a resource or document. The picker inserts a stable reference. Use the picker instead of typing an invented numeric ID.",
        "Keep references intact" to "Links use document IDs. Moving or renaming the target preserves the connection; existing written link labels remain as originally inserted. Linking a resource does not add it to the manuscript.",
        "Find references" to "Story references in the editor menu shows incoming links. Restore trashed targets through Settings → Recently deleted."
    )),
    GuideArticle("draft", "Writing", "Draft without getting stuck", listOf(
        "Start with a change" to "Write one sentence about who wants something, what prevents it and what might happen next. Begin the scene at a moment of pressure. Planning is optional.",
        "Keep moving" to "Use a short placeholder for a fact or transition you need to revisit. Keep unused dialogue, ideas and research as reference files. You do not need complete profiles before writing.",
        "Revise in passes" to "Check story order first, then scene purpose, then sentence clarity. Save a checkpoint before substantial revisions. Up to 20 manual checkpoints are kept per document."
    )),
    GuideArticle("editor", "Writing", "Formatting and saving", listOf(
        "Markdown" to "Use ## Heading, **bold**, *italic*, > quote and - bullet. The toolbar inserts syntax at the cursor or wraps selected text. Callouts use > [!NOTE]. The editor retains Markdown source.",
        "Your writing canvas" to "Focus mode hides extra controls. Settings → Editor controls writing text size, monospace text and syntax colors. The writing canvas scrolls as your text grows.",
        "Custom fields" to "Add optional project fields in Settings → Writing → Custom fields. In a document, open Page actions → Custom fields to enter text, numbers, dates, choices, URLs or document references. All values are optional. Fields and values travel with project recovery backups.",
        "Save state" to "Named files save locally after a short pause and before navigation; the top Save button also saves immediately. Untitled drafts are privately recovered on this device until you save them as files. When leaving, choose Save, Keep draft or Discard. One private draft is kept per workspace; New file resumes it until saved or discarded. Unsaved private drafts are not included in project backups. Saved locally means the database write succeeded. If saving fails, retry before leaving. Draft recovery is written before the normal autosave. Reopening a document recovers durable pending text; a conflicting saved version is kept and the recovery opens as a separate copy. An edit interrupted before the recovery write completes can still be lost.",
        "Undo and checkpoints" to "Undo and redo belong to the editing session. Checkpoints are persistent saved versions. Neither replaces an exported backup."
    )),
    GuideArticle("arrange", "Manuscript", "Arrange a nested manuscript", listOf(
        "Drag into order" to "Open Files, choose Outline options → Reorder files, then long-press a row by its drag handle, then drag over another row. Drop at the top edge to place it before, the bottom edge to place it after, or the middle of a folder to move it inside. The drag label shows the destination. Holding near the list edge scrolls the outline.",
        "Move between folders" to "Dropping beside an item adopts that item's parent folder. Use the actions menu → Move to choose a folder directly. Move up and Move down are alternatives to dragging. Folder moves cannot create loops.",
        "One manuscript" to "Write, manuscript counts and export use the same nested structure. All manuscript documents are included. Planning resources stay separate, even when referenced from the text.",
        "Delete safely" to "Trash moves a file or a folder and its contents to Recently deleted. Restore it in Settings. Deleting an entire workspace is permanent and requires confirmation."
    )),
    GuideArticle("safety", "Data and recovery", "Back up and restore your work", listOf(
        "Export a recovery backup" to "Use Back up workspace in the workspace menu, or Settings → Data & backup. Choose a destination outside the app with Android’s file picker. Back up before uninstalling or replacing development builds.",
        "What is included" to "Current backups preserve project metadata, folders, documents, reference/manuscript roles, cover images, custom type labels, trash, revision checkpoints and writing activity. App settings, bookmarks, old reader positions and unsaved drafts are not included. Older backups contain only the data supported by their format.",
        "Restore a copy" to "Use Import backup beside search in Library, or Restore from backup in Settings. Restore creates a separate project and reconnects internal links without overwriting the original.",
        "Export versus backup" to "Export manuscript produces the writing in nested manuscript order. It is not a recovery backup. There is no automatic cloud sync; Android’s picker can save to an available cloud destination."
    )),
)
