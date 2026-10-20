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
