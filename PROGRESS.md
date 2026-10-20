# NovelForge development checkpoints

## Objective
Native Android, offline-first novel-writing studio with a polished editorial UI.

## Stage 0 — initialized
- Repository confirmed empty on 2026-10-19.
- Android foundation is being implemented: Kotlin, Compose, Room, full-text search, local autosave, portable backup.
- Next: commit foundation, implement screens, compile and test, checkpoint remaining work.

Every stage is committed to main as requested. Build verification and limitations will be recorded here.

## Stage 1 — Android foundation
- Added Compose build configuration, Room schema, FTS search, scene-level persistence, story templates, checkpoint history, trash, and versioned portable JSON backup.
- ViewModel includes local autosave, export/import, undo/redo, project/entry actions, and optional demo data.
- This is a source checkpoint: UI and build validation follow in Stage 2.

## Stage 2 — Functional Compose UI
- Added editorial light/dark design, procedural cover editions, library grid/list, book metadata, project overview, chapters/arcs, story bible, and status board.
- Added Markdown-source writing with selection formatting, focus mode, undo/redo, stable-ID note links, guidance, checkpoints, and recoverable trash.
- Added continuous reader with appearance controls, contents, chapter navigation, and persisted scroll position.
- Added full-text search, settings, external backup/restore, and manuscript Markdown export.
- Gradle wrapper generated successfully. APK compilation, unit tests, lint and runtime checks are underway; this checkpoint is not yet a verified release.

## Stage 3 — Verification and continuity
- Added portable-backup unit tests, Android Room/FTS instrumentation test, and a Compose save-before-navigation test.
- Added GitHub Actions to compile, run unit tests and lint, and retain downloadable debug APK artifacts.
- Fixed imported stable note links, removed-arc backup references, empty-title persistence, and editor restoration after recreation.
- README documents build instructions, data safety, implemented scope, and deferred features.
- Local build and software-emulator validation are still running at this checkpoint; results will be recorded in the next commit.

## Stage 4 — Compile corrections and visual checks
- Corrected Material tab API opt-in, reader navigation icons, and a JVM setter collision in the theme ViewModel.
- Added a Robolectric native-rendering flow test for library, workspace, editor, reader, and story bible, with screenshot output.
- Removed transient Kotlin compiler session files from source tracking.
- Restored the local build toolchain after workspace tool caches expired. Clean compilation and validation are in progress; no APK is marked verified yet.

## Stage 5 — Reproducible build and visual corrections
- Upgraded Room to 2.7.2 and switched to KSP2 to resolve clean-build code generation incompatibilities.
- Fixed API 26 theme compatibility using version-qualified navigation bar resources.
- Fixed native screenshot capture in the Compose UI test. Improved text contrast and responsive cover typography.
- The previous session's uncommitted planning additions were not retained by the workspace. Restoring them from the documented checkpoint next.
- GitHub Actions is the verification authority for this checkpoint; final results follow.

## Stage 6 — Planning, connections and reader tools
- Added a reorderable timeline, event guidance template, wiki-link connection map/list, and incoming backlinks in story references.
- Added chapter bookmarks in the reader and contents list, plus selectable text.
- Fixed new-project synopsis persistence; formatted Kotlin sources.
- Added Room/FTS regression coverage to JVM tests and screenshot artifacts to CI.
- Build, tests and lint for this exact stage are pending CI completion.

## Stage 7 — Final regression pass
- Stage 5 passed GitHub Actions compilation, JVM tests and Android lint.
- Extended native UI navigation coverage to bookmark toggling and timeline-event creation.
- Fixed save status after undo returns to the persisted draft; deleted wiki-link targets now report a recoverable error.
- Connection refresh flushes pending edits before reading links.
- Final stage verification and APK packaging are underway.

## Stage 8 — On-device dialog regression
- JVM native-rendering checks passed the writing/reading flow and bookmark toggle, but timed out on dialog layout. The timeline creation interaction now runs as an Android instrumentation test; the JVM check retains timeline navigation/empty-state coverage.
- Added an accelerated API 34 emulator job to exercise the actual dialog and database behavior. Do not treat a compiled device test as a passing device test; final job results follow.

## Stage 9 — Verified APK delivered (2026-10-20)
- Source commit: 8accbf9e6f89992643ff9f390a7196ce542a50e1. CI run: https://github.com/iAdityaD/NovelForge/actions/runs/39361773110.
- Passed assembleDebug, assembleDebugAndroidTest, 10 JVM tests, Android lint (0 errors, 21 warnings), and 2 instrumentation tests on API 34.
- The on-device timeline-dialog test passed. The earlier dialog-idling timeout was limited to the JVM test run.
- Downloaded the CI APK, verified its package/minSDK and APK v2 signature, and saved the installable artifact.
- APK SHA-256: a253859cd4b4318e2973ad62270b7f2163fb3489e13fff9870357d4f6d06c429. Size: 16,844,410 bytes.
- Inspected actual UI renderings; committed bookshelf/editor/reader screenshots.
- Source is clean and checkpointed. README's Scope section is the remaining roadmap; do not claim a store-ready release, physical-device certification, or completed advanced export/media features.
- Next development should preserve Room schema compatibility and provide stable production signing before distribution.

## Redesign stage A — Workspace data model
- Added independent folder hierarchy, workspace status and daily writing activity, with explicit v1-to-v2 migration.
- Backup v2 preserves folders; v1 import remains supported. Cycle checks and subtree trash/restore protect workspace integrity.
- UI redesign specification is in docs/REDESIGN.md. Build validation follows after the replacement screens are connected.

## Redesign stage B — Modern shell and workspace UI
- Replaced primary navigation with Library, Guide, Analytics and Settings. Search is available from the Library toolbar.
- Added checkbox-based workspace creation and a collapsible file tree with file/folder creation, rename, move, drag-to-folder, and subtree trash.
- Added contextual editor drawer, syntax coloring, proportional/monospace preference, character metrics, quotes and callouts.
- Added pan/zoom/tap graph, practical Guide lessons, portfolio-vs-novel analytics, actual writing streak/activity, and category-based settings subpages.
- Changed visual language to compact sans-serif typography, charcoal/violet and cool light themes, geometric project covers and short crossfades. Verification and screenshots follow.

## Redesign stage C — Validation fixes
- Fixed cover sizing scope and explorer imports found by compilation.
- Committed Room v2's generated schema for migration testing.
- Preserved renamed/moved file metadata across later editor saves; protected trashed open files from resurrection.
- Updated system bars for both themes and added quote/callout rendering in reading mode.
- CI compilation, migration tests, device interactions and visual inspection are in progress.
