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

## Redesign stage D — Verified 0.2 delivery (2026-10-20)
- Source commit: 272686aaa8b749dd2fe66ec12e7ec46495a336c5. CI: https://github.com/iAdityaD/NovelForge/actions/runs/39430284995.
- Passed assembleDebug, testDebugUnitTest (14 tests), lintDebug (0 errors, 24 warnings), assembleDebugAndroidTest and connectedDebugAndroidTest (3 tests, API 34).
- Device checks cover FTS edits/trash, installed schema v1-to-v2 migration, and custom workspace creation with unchecked Lore, custom folder/file, writing/reading, move-to-root and portfolio/novel analytics filtering.
- Fixed asynchronous test navigation waits and library card text clipping. Inspected native-rendered creation, Library, explorer, editor, analytics, Guide and Settings screenshots.
- APK 0.2.0, versionCode 2, minimum API 26; verified APK signature. Size 17,024,634 bytes. SHA256 e6bd06c03240db46a92f28f85b0d6db87758a5e5f7a9387526c64dd5c5694264.
- Packaged APK and installation instructions as NovelForge-v0.2-Android.zip to avoid the earlier direct APK attachment download issue.
- Confirmed old 0.1 and new 0.2 debug signing certificates differ. Users must export all project backups before uninstalling the older build; reinstall/restore does not preserve revisions, bookmarks or activity history. Production signing remains future work.
- README and docs/REDESIGN.md describe the new four-tab UI, workspace hierarchy, data semantics and remaining limits. No further code changes after the verified commit; final checkpoint only updates documentation/screenshots.
- Next: stable release signing, physical-device performance/accessibility review, and the explicitly listed export/media roadmap. Do not imply those are completed.

## 0.3 stage A — Modular architecture
- Extracted pure Kotlin model and Workspace, Writing, Story, Analytics, Backup and Preferences domains.
- Added Room entity/domain mappings and repository implementations in data:local, with Hilt wiring.
- Replaced ForgeViewModel with feature-specific ViewModels and read-only StateFlows; a shared pure Kotlin editing session coordinates autosave and explorer mutations.
- Extracted eight UI feature modules, a design system and a small presentation foundation. Removed obsolete screens.
- Added typed destination enum and CI checks against forbidden module dependencies.
- Database canonical name, tables and schema version remain compatible at this stage. Build validation underway; next stage adds genres, limits, creation controls and the explorer fixes.

## 0.3 stage B — Metadata and explorer improvements
- Full-screen novel details replaces the overflowing dialog. Synopsis uses a bounded, internally scrolling editor, scrollbar, word counter and explicit oversized-input feedback. Unsaved edits require discard confirmation.
- Settings → Writing → Content limits controls the global synopsis limit (50–10,000, default 500). Existing over-limit text can be retained or shortened; imports preserve it intact.
- Genres are distinct domain values, normalized in BookGenre rows and backed up in format v3. Legacy genre strings are preserved verbatim as one value. Library supports genre filters.
- Creation supports adding, renaming and removing custom starter folders. Shared policies reject invalid or duplicate folder names.
- Explorer now uses flat compact rows, independent expand/select actions, search, expand/collapse all, active-file highlighting and reveal, plus drawer create/close controls.
- Frozen migration rules preserve schema history; v2→v3 migration only adds genre storage.
- Stage A compiled and passed JVM/lint; emulator startup failed before app execution. New migration and device cases added for stage B validation.

## 0.3 stage C — Build verified; device run pending
- Source 7d004b89de5704b16767fa8be2b348274b748962 passes architecture boundaries, compilation, 19 JVM tests and lint (0 errors, 14 warnings).
- CI: https://github.com/iAdityaD/NovelForge/actions/runs/39495977321. Device-test job 118881003493 was still running at this checkpoint; do not report emulator success until its result is inspected.
- Fixed shared status definitions after narrowing feature dependencies.
- Inspected native-rendered creation and drawer screenshots; saved refreshed repository screenshots and docs/ARCHITECTURE.md. Generated Room schema 3 is retained for migration testing.
- Built APK 0.3.0/versionCode 3/minSDK 26. Signature verified. SHA256 a439363648d2897f2c7760e672bcc0227ed00420c8aa8ecad16b4550c406c4bc, 17,460,642 bytes. Final packaging waits for device verification.
- Next: inspect device tests and their synopsis/limits screenshots, fix any failures, package APK/installation ZIP and update final validation record.


## 0.3 stage D — Verified delivery (2026-10-21)
- Final app source: 2e7911d4c92910113de2cb05bedc700b582ca451. CI: https://github.com/iAdityaD/NovelForge/actions/runs/39533043838.
- Passed compilation, module boundaries, 19 JVM tests, lint (0 errors, 14 warnings), and all five API 34 instrumentation tests with no failures/skips.
- Device tests cover v1→v3 and v2→v3 migration, FTS, custom starter folders, genre persistence, synopsis overflow rejection/save/reopen, settings limits, writing/reading, file moves and analytics filters.
- Corrected test synchronization: scroll to the synopsis section label, use a stable first workspace action while data loads, scope move destination to its dialog, bound test runtime, and retain screenshots in shared test media before UTP app cleanup.
- Visual inspection found keyboard panning in the full-screen dialog. Novel details now uses the Activity layout; verified title, bounded synopsis/scrollbar and Save changes remain visible above the keyboard. Test asserts header/save visibility.
- Captured and inspected synopsis, limits and custom-folder screens; repository screenshots and architecture documentation are current.
- APK: 0.3.0, versionCode 3, minSDK 26, 17,460,642 bytes. SHA256 d16353d0fe924f81b72dea44b9b7edbd77cc4b5485351127101f292f44417576. APK signature verified; certificate SHA256 9f0ae48f8c91fffee48b12277e549c308a1f7e6a6aeb16ed7076138d637481f5.
- Delivery: NovelForge-v0.3-Android.zip contains APK and INSTALL.txt. Export every novel before uninstalling an older debug build with a different signing key; portable backups exclude revisions, trash, bookmarks and activity history.
- No application code changes after the verified source. Remaining work is the documented future roadmap: stable production signing, physical-device performance/accessibility review, and export/media capabilities. Requested 0.3 architecture/metadata/explorer upgrade is complete.


## 0.4 stage A — Beginner workspace and focused controls (2026-10-22)
- Added standard/from-scratch creation, stable-key starter-folder preferences, eight editable README guides, two manuscript chapters, and optional chapter/character/novel-profile templates. Standard creation is transactional and opens the overview by document ID.
- Settings → Writing → Workspace structure customizes folders for future novels. Existing folders and documents are not migrated or rewritten.
- Workspace pencil opens metadata and status editing; synopsis is retained in storage but removed from this form. Overflow contains export, backup and confirmed whole-workspace deletion.
- Deletion removes workspace data, revisions, genres, FTS content and activity, coordinates with the editing session and clears local reading references.
- Library uses a combined status/genre filter dialog and header import action. Domain boundaries retained; no new dependencies or schema version changes.
- Implementation checkpoint only: update tests, run full CI and inspect screenshots before delivery.

## 0.4 stage B — Verification coverage
- Added repository tests for standard/scratch creation, customized folder roles, persistent IDs across rename/move/reopen, guide backup compatibility, duplicate validation, and deletion isolation/cleanup.
- Updated device flows for template settings, automatic overview reading, metadata/status editing, combined library filters, recreation, deletion cancel/confirm and empty workspace writing.
- Architecture boundaries and whitespace checks pass locally. Full build, JVM, lint and emulator verification are now requested in CI.

## 0.4 stage C — Verified delivery (2026-10-22)
- App source: eef9834e796ddb90992882f5b993cdccf29628e2. CI: https://github.com/iAdityaD/NovelForge/actions/runs/39831366795.
- Passed architecture boundaries, compilation, 24 JVM tests, five API 34 device tests, and lint (0 errors, 14 warnings). No failed/skipped tests.
- Inspected actual library, creation, metadata, filter, guide, structure settings and deletion screens. Refreshed repository screenshots and architecture/walkthrough documentation.
- Verified APK 0.4.0/versionCode 4/minSDK 26 and signing certificate. Size 17,477,026 bytes; SHA256 d898053781e125200bd1b1658272579fb275dd798166521dec1c62f788b84dc2. Certificate SHA256 3ec994ec210e991cb48cd900bf1940b848ba40a4bd8c149d2404fb6bce851cf2.
- Packaged NovelForge-v0.4-Android.zip with APK and installation/backup instructions. Debug certificate differs from 0.3; export every workspace before uninstalling.
- Requested 0.4 changes complete; existing workspace structures and legacy synopsis metadata remain intact. No application changes after verified source; this checkpoint updates documentation/screenshots only.
- Future work remains production signing, physical-device performance/accessibility review, and the documented export/media roadmap.

## 0.5 stage A — Workspace shell and manuscript order
- Added Overview, Plan, Write, Arrange and Read views; lifted the explorer into a shared drawer available in workspace, editor and reader.
- Added plain Kotlin domain:manuscript, Room v4 additive placement storage and v3→v4 migration. Chapter manuscript order/inclusion is independent of folder position; unplaced chapters retain bodies and links.
- Reading contents and Markdown export consume manuscript order; portable backup v4 preserves placement/inclusion and reads v1–v3.
- Updated navigation/device coverage and added persistence/placement regressions. Architecture and whitespace checks pass; CI and migration validation pending.
- This is the first incremental upgrade, not the complete roadmap. Scene nesting, shared series, advanced exports, full-snapshot backups and production signing remain future stages.

## 0.5 stage B — Stable overview and migration/restore coverage
- Added stable overview document reference to Book and migration recovery for existing generated profiles, including renamed folders. Portable restore remaps the overview ID.
- New chapters start as blank prose, preventing instructional template text from entering manuscript counts/export. Existing text is never rewritten.
- Added Theme and Prop planning types, v3→v4 installed-database verification and actual backup/restore/export regression coverage.
- Full CI validation pending; release scope remains the first workspace/manuscript increment.


## v0.5 verified delivery — 2026-10-23
- Source: `5c7119897a260126ec3433b8b4cc5a41b419a11f`; CI `39921422025`, both jobs successful.
- 27 JVM tests, 6 API 34 device tests; lint 0 errors / 14 warnings. APK signature verified; version 0.5.0 (5), min SDK 26.
- Delivered first workspace increment: Overview / Plan / Write / Arrange / Read, shared explorer, independent chapter placement, non-destructive unplacing, stable overview ID, Room and portable backup v4.
- Packaged APK and installation/data-preservation instructions as NovelForge-v0.5-Android.zip.
- Visually checked native overview and device Arrange captures. Immediate post-creation device screenshot still catches IME closing; physical-device keyboard-transition verification remains pending. Do not treat semantic visibility assertion as proof of keyboard occlusion clearance.
- Remaining roadmap: nested scenes/parts, typed relationships, shared worlds/series, richer export, full snapshot backups, stable signing; see docs/WORKSPACE_UPGRADE_PLAN.md. Do not call the full roadmap complete.


## 0.5.1 — Folder-backed resources and workspace corrections
- Plan now lists resources grouped by actual folders. Removed the fixed kind dropdown; the single + opens category selection/creation. Scratch starts without categories, standard uses existing structure. Legacy root files remain visible as Uncategorized.
- Resource creation is one database transaction: category validation, optional folder, document and parent ID. New Resource / External source document kinds remain compatible with the existing backup codec, editor and stable wiki links. External sources retain URL and notes, without downloading articles.
- Overview replaces Recent documents with a read-only synopsis and dedicated bounded editor respecting Settings content limits. Write creation only offers manuscript types.
- Consume Scaffold insets before the nested workspace navigation bar to avoid duplicate bottom-system padding.
- Added repository regression and device coverage. CI pending. No schema migration or unrelated rewrite required.


## 0.5.1 verified delivery
- Source `87f20877e4d8d996fd538f7307e52200d7585bd6`; CI `39926424532`: build and device jobs successful. 28 JVM tests / 6 API 34 tests, no failures or skips. Compilation, architecture checks and lint passed.
- APK signature verified, versionCode 6 / versionName 0.5.1 / minSDK 26. Packaged NovelForge-v0.5.1-Android.zip.
- Inspected Plan and Arrange device captures for resource layout and consumed bottom insets. Physical-device review remains outside emulator verification.
- No automatic relocation of old files, schema rewrite, or downloaded web content. Categories are logical folders; source links and notes use canonical documents. Future full-roadmap stages remain in WORKSPACE_UPGRADE_PLAN.md.
