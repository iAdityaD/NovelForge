# NovelForge — local Android writing workspace

NovelForge 0.2 organizes each novel like a coding project: an independent workspace with a flexible folder tree, Markdown pages and linked story references. Built with Kotlin, Jetpack Compose and Room. The redesigned interface uses compact sans-serif typography, charcoal/violet and light themes, geometric covers and short transitions.

## Screens

Actual Compose screens captured by the automated native-rendering test.

| Library | Workspace | Analytics |
| --- | --- | --- |
| ![Library](docs/screenshots/bookshelf.png) | ![Workspace](docs/screenshots/workspace.png) | ![Analytics](docs/screenshots/analytics.png) |

| Create workspace | Editor | Settings |
| --- | --- | --- |
| ![Creation](docs/screenshots/creation.png) | ![Editor](docs/screenshots/editor.png) | ![Settings](docs/screenshots/settings.png) |

See [the UI/UX walkthrough](docs/REDESIGN.md) for each screen and its interactions.

## Navigation and features

- **Library:** grid/list workspaces, covers, status filters, word goals, continue writing and global full-text search.
- **Guide:** five practical offline lessons on workspace structure, linking characters/worlds, scene planning, revision and backup.
- **Analytics:** writing streak above the workspace dropdown. All workspaces shows portfolio totals by status; one workspace shows manuscript progress, chapter states and story-bible statistics. Both include words, estimated pages, chapters, character profiles and daily activity.
- **Settings:** a category index leading to separate Appearance, Editor, Reading, Data & backup, Recently deleted and About screens.
- **Workspace setup:** optional checkbox templates for Chapters, Characters, Lore, Plot and Metadata. Add any nested folders and files later.
- **Explorer:** expand/collapse, rename, move, sibling ordering, long-press drag onto folders, and recoverable subtree deletion. File type remains independent of folder location.
- **Writing:** autosave, syntax coloring, proportional/monospace choice, formatting toolbar, wiki-link picker, focus mode, undo/redo, checkpoints, backlinks and word/character/reading-time metrics.
- **Reading:** typography controls, warm paper, contents, bookmarks, remembered position and fullscreen controls.
- **Connections:** pan/zoom/tap graph plus a textual link list. Graph display caps at 40 nodes; the list retains all references.
- **Local-first data:** Room/SQLite, FTS search, portable JSON backups and Markdown manuscript export. No account, network permission or remote analytics service.

The explorer is a logical tree in the local database, not an external Markdown vault. Pages are estimated at 250 manuscript words. Writing history starts with 0.2; old streaks are not invented. Word additions measure positive net changes between saves.

## Verification and installation

[CI run 39430284995](https://github.com/iAdityaD/NovelForge/actions/runs/39430284995) passed for source `272686aaa8b749dd2fe66ec12e7ec46495a336c5`: 14 JVM tests, 3 API 34 emulator tests, compilation and lint (0 errors, 24 warnings). APK signature, package/version and minimum Android version were verified. APK size is 17,024,634 bytes. GitHub Actions publishes the debug APK as the `NovelForge-debug` artifact. Download and unzip it, then open the APK on Android 8.0 or newer.

**Before replacing an older development build, export each workspace backup outside app storage.** CI debug signing keys can differ between builds. If Android rejects the update because of its signature, only uninstall after securing your backups; install the new APK and restore them. Uninstalling deletes app-local data. Production distribution still requires a stable private signing key.

## Run

Open this repository in Android Studio, use JDK 17, install Android SDK 35, and run the `app` configuration. Android 8.0 (API 26) or newer is required.

```sh
./gradlew assembleDebug
./gradlew testDebugUnitTest lintDebug
```

The development APK is `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions builds and retains this APK after each successful push to main. Debug builds are for evaluation, not store distribution. Configure your own signing key for release builds.

## Data and safety

Room is the source of truth. Library and outline screens load summary projections; full text is loaded when opening a page. Edits are saved after a 400 ms pause, flushed before navigation, and flushed when the activity stops. A sudden process kill during the unsaved debounce window can still lose that latest fragment; the UI displays `Saving…` until persistence completes. Explicit save failures block editor navigation.

Project backups contain current active pages, metadata, and story structure. Restore creates a new project in one transaction, remapping page IDs, arc references, and wiki links. Imports are bounded to 16 MB / 10,000 entries and validate the format version and relationships. Backups do **not** include manual revisions, trash, bookmarks, reading positions, or appearance settings. Export outside app storage before uninstalling or changing devices. Android cloud backup is disabled deliberately.

No destructive database migrations are enabled. Schema exports live under `app/schemas`; future schema changes need explicit migrations and restore testing.

Room schema v2 migrates installed v1 databases without destructive fallback, preserving entry IDs, revisions, arc references and full-text search, and organizing existing pages into default folders. Backup v2 includes folder hierarchy and project status; v1 backups remain importable. Backup imports do not restore writing streak history.

## Limits and next work

This is a functional development build. Physical-device frame-time, large-font and screen-reader certification remain pending. The screenplay template does not implement professional screenplay pagination. Reader rendering supports a Markdown subset rather than full CommonMark.

Future work includes imported cover/media attachments, general Markdown-vault import, EPUB/PDF/DOCX export, reader pagination/highlights/TTS, revision diffs, richer link aliases, split-pane tablet navigation, scheduled external backups and release signing. Backups use Android's document picker; automatic cloud sync is not implemented.

## Resume work

Read `PROGRESS.md` and `docs/REDESIGN.md` first. Preserve existing data and user edits; commit each completed stage as requested by the owner.
