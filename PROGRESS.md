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
