# Workspace redesign — 0.2

Each novel is an independent local workspace. Folders are user-controlled, not rigid categories. A file's type (chapter, character, lore, etc.) remains independent of its location, keeping analytics accurate when authors reorganize a world.

## Navigation
- Library: compact project cards, progress, activity, search, grid/list switch and a persistent create action.
- Guide: practical lessons for creating a novel, planning scenes, wiki links, revisions and backup.
- Analytics: a writing streak and activity strip above a workspace selector. All workspaces shows the portfolio (novels by status and combined manuscript totals). A selected workspace shows its manuscript progress, chapter states and story-bible counts. Pages are explicitly estimates at 250 manuscript words/page.
- Settings: category index with separate Appearance, Editor, Reading, Data & backup, Trash and About pages.

Opening a workspace hides the global tabs and presents a file explorer. The root contains only the folders selected in creation; folders can be added, nested, renamed, moved and trashed later. Long-press and drag onto a folder relocates an item; a Move action provides an accessible alternative. Folder deletion includes descendants and remains recoverable.

The editor uses a compact file header and breadcrumb, source Markdown with optional syntax color, a keyboard-friendly toolbar, wiki-link picker, save status and metrics. Reading mode retains only a small control strip which can be hidden. The graph links to actual files and supports selecting nodes.

## Data contract
- Room v1 → v2 migration preserves every existing entry ID, arc assignment, revision and FTS record, and organizes old pages into default folders.
- Backup format v2 preserves hierarchy and project status. v1 backups still import. Invalid folder references/cycles are rejected before writing.
- Activity begins with this version; historical streaks are not fabricated. A day counts when text edits are persisted; words added counts positive net word changes between saves.
- Local database is the source of truth; the explorer is a logical folder tree, not direct access to arbitrary Android filesystem paths. Exports use the Android document picker.

## Screen-by-screen walkthrough

### Library
The landing page has a title, global search action, status filters, a Continue writing card and a workspace grid/list toggle. Project cards use lightweight geometric covers, title, genre, manuscript-word progress and last-edited date. New workspace stays available as a floating action. Empty results explain whether there are no novels or simply no novels matching the selected status.

### Create workspace
Creation is a full screen, so the keyboard does not compete with a cramped dialog. Enter title, author and genre; select a color; check or uncheck each starter folder. No folders are mandatory. Selecting Chapters creates a first chapter; selecting Metadata creates a novel-overview note. Authors can add missing folders later without recreating a novel.

### Workspace explorer
The header identifies the novel and provides details, completion status, export and backup actions. Files and Graph are local views inside the workspace; they do not replace the four global navigation tabs. The root row clears the current folder selection. Tapping a folder expands/collapses it and sets the destination for new files and folders. The tree shows file type and word count beside its Markdown title.

A file menu offers rename, move to another folder/root, sibling ordering and recoverable trash. Dragging after a long press moves an item onto a folder. Invalid self/descendant folder moves are rejected. Trashing a folder includes its subtree; restoring it brings the subtree back. Restoring one child also restores its required ancestor folders.

### Editor
The left toolbar action opens a contextual drawer containing the same workspace tree. A compact breadcrumb returns to the workspace. File title, status, save state, word count, character count and reading-time estimate keep orientation visible. The horizontal toolbar supports bold, italic, headings, lists, checklists, quotes, callouts and wiki-link selection. Source Markdown remains editable; syntax colors do not change text or cursor positions.

Focus mode reduces chrome. Checkpoints and references live in the overflow menu. A save failure remains visible and prevents normal editor navigation until retried. Moving or renaming the open file updates the editor snapshot, preventing later autosave from overwriting the operation.

### Reading and graph
Reading mode uses independent serif/proportional and warm-paper controls, chapters/contents, saved position and bookmarks. Fullscreen hides the navigation strips, leaving an explicit Show controls action. The graph supports pan, pinch zoom, reset and node selection. The textual references below it provide an alternative to precision tapping and expose all links when the visual map is capped at 40 nodes.

### Guide
Five offline lessons cover workspace structure, linking characters/worlds, outlining and scene construction, drafting/revision, and backup. Each lesson opens as a dedicated reading page with a clear return action. Examples explain actual stable wiki-link syntax and the difference between temporary undo and durable checkpoints.

### Analytics
The streak card stays above the workspace filter. With All workspaces selected, it displays activity across novels plus workspace totals by In progress, Completed and On hold. Selecting a novel replaces the portfolio section with manuscript-goal progress, project status, a chapter drafting pipeline, lore-file totals and all-file totals. Both scopes include manuscript words, estimated pages, chapters, character profiles and seven-day word additions. Character profiles are distinct from text-character count in the editor. Zero activity produces a truthful empty streak and zero-height chart, not seeded demo history.

### Settings
The index only lists categories. Appearance controls light/dark theme; Editor controls proportional/monospace, syntax colors and shared text size; Reading controls reading typography and paper. Data & backup has a workspace selector and explicit export/restore actions. Recently deleted provides restoration. About states version and the local-storage model. Android Back returns from a settings category to the index before returning to Library.

## Interaction and validation expectations
- 48 dp icon-button targets, labels for icon actions, scrollable content and keyboard-aware forms.
- Short 160 ms screen fades rather than large animated transitions; writing text does not trigger screen transitions.
- The graph and drag gestures have list/menu alternatives.
- Dark/light system-bar contrast follows the app theme.
- Persisted records, not decorative cards, drive analytics and progress.
- Native rendering, emulator interaction and database migration are tested in CI. Physical-device frame-time, screen-reader and large-font certification must not be inferred from those checks.
