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
