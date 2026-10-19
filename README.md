# NovelForge — Android writing studio

An offline Android app for drafting novels, organizing a story bible, and reading your manuscript. Kotlin + Jetpack Compose + Room. Editorial ivory/forest design, dark mode, and lightweight procedural covers.

## Run

Open this repository in Android Studio, use JDK 17, install Android SDK 35, and run the `app` configuration. Android 8.0 (API 26) or newer is required.

```sh
./gradlew assembleDebug
./gradlew testDebugUnitTest lintDebug
```

The development APK is `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions builds and retains this APK after each successful push to main. Debug builds are for evaluation, not store distribution. Configure your own signing key for release builds.

## Working features

- Local bookshelf with grid/list layouts and four cover editions.
- Create novels, edit title/author/genre/synopsis/word goal.
- Chapters, arcs, ordering, arc assignment, and a drafting-status board.
- Character, location, backstory, worldbuilding, plot, research, screenplay, and free-note templates.
- Markdown-source editor, selection formatting, focus mode, grouped undo/redo, autosave and visible failure handling.
- Stable-ID wiki links (`[[123|Mara]]`), rendered as tappable links in reading mode.
- Twenty manual revision checkpoints per page; recoverable deleted pages.
- Continuous reader, contents, previous/next chapters, serif/sans, warm paper, adjustable type, remembered scroll position.
- Room FTS4 full-text search across all books, excluding trash.
- Portable JSON project backup/restore and Markdown manuscript export through Android's document picker.
- Optional sample project; no sample data is silently inserted.
- No INTERNET permission, accounts, analytics, or remote services.

## Data and safety

Room is the source of truth. Library and outline screens load summary projections; full text is loaded when opening a page. Edits are saved after a 400 ms pause, flushed before navigation, and flushed when the activity stops. A sudden process kill during the unsaved debounce window can still lose that latest fragment; the UI displays `Saving…` until persistence completes. Explicit save failures block editor navigation.

Project backups contain current active pages, metadata, and story structure. Restore creates a new project in one transaction, remapping page IDs, arc references, and wiki links. Imports are bounded to 16 MB / 10,000 entries and validate the format version and relationships. Backups do **not** include manual revisions, trash, reading positions, or appearance settings. Export outside app storage before uninstalling or changing devices. Android cloud backup is disabled deliberately.

No destructive database migrations are enabled. Schema exports live under `app/schemas`; future schema changes need explicit migrations and restore testing.

## Scope of this build

This is a functional first Android build, not a claim that the entire product roadmap is finished. The screenplay editor currently provides a monospace template and scene-heading insertion, not industry-standard screenplay pagination/export. Reader rendering supports headings, emphasis, bullets/checklists and wiki links, not the full CommonMark specification.

Still to implement: user-imported cover/media attachments, image deduplication, WYSIWYG editing, general Markdown-folder import, EPUB/PDF/DOCX export, reader pagination/highlights/bookmarks/TTS, automatic revision diffs, timeline/relationship graph, stable backlinks and alias management, richer scene/volume hierarchy, split-pane tablet navigation, automatic external backup scheduling, local model integrations, release signing and physical-device performance/accessibility certification.

## Resume work

Read `PROGRESS.md` first. It records stage commits, validation, limitations, and the next work. Preserve existing data and user edits; commit each completed stage as requested by the owner.
