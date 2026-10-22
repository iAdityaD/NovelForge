# NovelForge — local Android writing workspace

NovelForge 0.4 organizes each novel like a coding project: an independent workspace with a flexible folder tree, Markdown pages and linked story references. Built with Kotlin, Jetpack Compose and Room. The redesigned interface uses compact sans-serif typography, charcoal/violet and light themes, geometric covers and short transitions.

## Screens

Actual Compose screens captured by the automated native-rendering test.

| Library | Workspace | Analytics |
| --- | --- | --- |
| ![Library](docs/screenshots/bookshelf.png) | ![Workspace](docs/screenshots/workspace.png) | ![Analytics](docs/screenshots/analytics.png) |

| Create workspace | Editor | Settings |
| --- | --- | --- |
| ![Creation](docs/screenshots/creation.png) | ![Editor](docs/screenshots/editor.png) | ![Settings](docs/screenshots/settings.png) |

| Editor explorer |
| --- |
| ![Explorer drawer](docs/screenshots/drawer.png) |

API 34 device captures:

| Details | Library filters | Overview guide |
| --- | --- | --- |
| ![Details](docs/screenshots/workspace-details.png) | ![Filters](docs/screenshots/library-filter.png) | ![Guide](docs/screenshots/overview-guide.png) |

See [the UI/UX walkthrough](docs/REDESIGN.md) for each screen and its interactions.

## Architecture

A domain-based modular monolith with Clean Architecture boundaries, Hilt dependency injection and feature-specific MVVM/StateFlow. Plain Kotlin domains define contracts and rules; local repositories implement persistence; UI modules consume domain interfaces. See [ARCHITECTURE.md](docs/ARCHITECTURE.md) for module ownership, dependency direction and the steps for adding a feature.

## Navigation and features

- **Library:** grid/list workspaces, covers, combined status/genre filter dialog, header backup import, word goals, continue writing and global full-text search.
- **Guide:** five practical offline lessons on workspace structure, linking characters/worlds, scene planning, revision and backup.
- **Analytics:** writing streak above the workspace dropdown. All workspaces shows portfolio totals by status; one workspace shows manuscript progress, chapter states and story-bible statistics. Both include words, estimated pages, chapters, character profiles and daily activity.
- **Settings:** a category index leading to separate Appearance, Editor, Reading, Data & backup, Recently deleted and About screens.
- **Workspace setup:** Standard structure or Build from scratch. Standard creates eight beginner-friendly folders, editable README guides and two optional-template chapters; scratch starts empty. Customize future starter folders in Settings → Writing → Workspace structure.
- **Explorer:** compact flat rows, separate expand/select controls, search, expand/collapse all, active-file reveal, rename, move, sibling ordering, drag-to-folder and recoverable subtree deletion. The editor drawer includes create actions and an explicit close button.
- **Novel details:** a separate pencil action opens title, author, multiple genres and writing status. Synopsis belongs in the overview document. Overflow contains export, backup and confirmed permanent workspace deletion.
- **Writing:** autosave, syntax coloring, proportional/monospace choice, formatting toolbar, wiki-link picker, focus mode, undo/redo, checkpoints, backlinks and word/character/reading-time metrics.
- **Reading:** typography controls, warm paper, contents, bookmarks, remembered position and fullscreen controls.
- **Connections:** pan/zoom/tap graph plus a textual link list. Graph display caps at 40 nodes; the list retains all references.
- **Local-first data:** Room/SQLite, FTS search, portable JSON backups and Markdown manuscript export. No account, network permission or remote analytics service.

The explorer is a logical tree in the local database, not an external Markdown vault. Pages are estimated at 250 manuscript words. Writing history starts with 0.2; old streaks are not invented. Word additions measure positive net changes between saves.

## Verification and installation

The 0.4 build at source `eef9834e796ddb90992882f5b993cdccf29628e2` passes compilation, architecture boundaries, 24 JVM tests, five Android API 34 device tests, and lint (0 errors, 14 warnings). [CI run 39831366795](https://github.com/iAdityaD/NovelForge/actions/runs/39831366795) retains the APK, reports and device screenshots. Coverage includes migrations, FTS, standard/scratch creation, customized starter roles, identity persistence, backup compatibility, deletion isolation, metadata/status editing, combined filters, writing/reading and analytics. Captured screens were visually inspected.

GitHub Actions publishes the debug APK as the `NovelForge-debug` artifact. Download and unzip it, then open the APK on Android 8.0 or newer. The packaged download is `NovelForge-v0.4-Android.zip` (APK plus installation instructions). APK SHA256: `d898053781e125200bd1b1658272579fb275dd798166521dec1c62f788b84dc2`.

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

Room schema v3 migrates installed v1/v2 databases without destructive fallback, preserving entry IDs, revisions, arc references and full-text search, and organizing existing pages into default folders. Backup v3 includes multiple genres, folder hierarchy and project status; v1/v2 backups remain importable. Schema v3 adds normalized BookGenre storage, preserving legacy genre strings as one value. Over-limit restored synopses remain intact. Backup imports do not restore writing streak history.

## Limits and next work

This is a functional development build. Physical-device frame-time, large-font and screen-reader certification remain pending. The screenplay template does not implement professional screenplay pagination. Reader rendering supports a Markdown subset rather than full CommonMark.

Future work includes imported cover/media attachments, general Markdown-vault import, EPUB/PDF/DOCX export, reader pagination/highlights/TTS, revision diffs, richer link aliases, split-pane tablet navigation, scheduled external backups and release signing. Backups use Android's document picker; automatic cloud sync is not implemented.

## Resume work

Read `PROGRESS.md` and `docs/REDESIGN.md` first. Preserve existing data and user edits; commit each completed stage as requested by the owner.
