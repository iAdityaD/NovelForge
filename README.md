# NovelForge — local Android writing workspace

NovelForge 1.0.0-rc2 is a local-first writing app built with Kotlin, Jetpack Compose, Room, Hilt and Coroutines/Flow. The workspace tabs are **Overview, Write, AI**. Write opens documents directly, with the workspace tree in Files; the owl AI tab displays Coming soon. Plan, Arrange and Read are no longer separate tabs.

## Current workflow

- **Library:** grid/list projects, search, combined status/genre filters, backup import and creation.
- **Create:** title required; author, multiple genres and color optional. Start blank or use an optional Novel, Short story, Screenplay, Nonfiction or Essay recipe. Generated folders remain editable. Customize Novel starter folders in Settings.
- **Overview:** project identity, editable synopsis, resume writing and manuscript totals. Project metadata uses a separate pencil action. Export, backup and confirmed workspace deletion remain in the menu.
- **References:** nested folders and documents for ideas, notes, research and sources. Attach link accepts a name, URL and destination, with an optional new folder. URLs are stored with notes, not downloaded.
- **Write:** first visit offers New file and Attach link; later visits resume the last selected document. New file opens an Untitled draft outside the explorer until saved. The top Save action chooses filename and destination on first save; named files also autosave. Read / Write toggles the current document without another workspace tab.
- **Files:** the whole logical workspace, compact collapsible rows, file search, custom type/status filters, rename/duplicate/move/trash and scroll position indicators. Filters preserve ancestor folders. Outline options → Reorder files reveals drag handles; before/inside/after placement and edge scrolling arrange the canonical tree. Reordering is disabled while filtering.
- **Editor:** Markdown, syntax colors, typography, focus mode, stable internal links, backlinks, undo/redo, checkpoints, custom type labels/statuses, word counts and truthful autosave state.
- **Guide:** searchable offline articles matching the current workflow, including resources, writing, nested order, linking and recovery.
- **Analytics:** streak and project selector, all-project portfolio totals or selected-project metrics, manuscript words/pages/documents, resources and activity.
- **Settings:** focused category subpages for Appearance, Editor, Writing, Data & backup, Recently deleted and About. Writing includes default folders and the synopsis word limit. File-level reading uses the existing typography preferences.

The UI uses charcoal/violet and light themes, compact rows and scrollable forms. Templates are optional starting content, not enforced taxonomies. A document's stored reference/manuscript role is independent of its editable title and type label.

## Data model and architecture

The existing Android stack is retained. A domain-based modular monolith separates pure Kotlin models/contracts, application services, Room repositories, feature ViewModels and Compose UI. No SQL belongs in UI components. See [architecture](docs/ARCHITECTURE.md) and [the current product specification](docs/PRODUCT_REDESIGN.md).

Room schema **7** adds locally stored cover images. Schema 6 added optional custom field definitions and values. Schema 5 added the explicit document area while retaining IDs, content, references, folders and revisions. Legacy mixed folders are accessible together in Write; migration does not silently reorganize or delete their text. Prior manuscript exclusion records remain for historical compatibility, but no longer hide documents from the current manuscript. Nested folder/sibling order is now shared by Write, export and manuscript statistics.

`WorkspaceRepository.createNode` creates generic documents/folders and optional resource categories transactionally. `placeBefore` validates project ownership and cycles, then changes parent/sibling order atomically. `orderedManuscript` is the iterative depth-first projection used for canonical manuscript order. Type labels and document statuses remain portable freeform values; project-owned field definitions support optional typed metadata. Field types are immutable after creation; used choices cannot be removed.

## Storage, backup and recovery

Everything works without an account or network connection. The app has no internet permission. The explorer is a logical SQLite tree, not a live external Markdown vault.

Recovery backup **v8** preserves project metadata and cover images, documents, custom labels/statuses, Plan/manuscript roles, folder order, trash, checkpoints and writing history. Restore validates bounded input and creates a separate project in one transaction, remapping IDs and internal links. Legacy v1–v7 backups remain supported. Markdown export contains the complete current manuscript in nested order, excluding Plan resources.

Project backups do **not** include app preferences, historical reader bookmarks/positions or pending unsaved drafts. Limits: 16 MB and 10,000 documents per backup. Export before uninstalling or changing devices. Android cloud backup is disabled.

An untitled draft is privately retained per workspace. Leaving offers Save / Keep draft / Discard. First Save creates the destination folder and document transactionally; retries cannot duplicate it. New file resumes a kept draft until it is saved or discarded.

Autosave runs after a short pause and flushes on navigation/backgrounding. Saved locally follows successful persistence. Save failures block normal editor departure. An atomic recovery journal is written before the debounced database commit. Reopening restores pending text, or preserves it as a separate copy if the saved document has diverged. An interruption before the journal write finishes can still lose the latest fragment.

## Build and installation

Use JDK 17 and Android SDK 35. Android 8.0/API 26 or newer is required.

```sh
python3 scripts/check_architecture.py
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
```

GitHub Actions runs API 34 and API 36 emulator journeys, migrations, unit tests, debug/release lint, architecture checks and a minified release-bundle build. Candidate `1.0.0-rc1`, source `f612f84c997f7e4e052f8f99f0c0e466c54d9b43`, passed 45 JVM tests and 9 tests on each Android version in [CI run 40169296217](https://github.com/iAdityaD/NovelForge/actions/runs/40169296217). The APK is debug-signed and the release App Bundle is unsigned. See [PROGRESS.md](PROGRESS.md) for hashes and artifacts, and [release preparation](docs/RELEASE.md) for publishing gates.

**Before replacing an older development build, export every project outside app storage.** CI debug signatures may differ. If Android rejects an update signature, do not uninstall until backups are secured. Uninstalling deletes local data. Production distribution requires an owner-controlled stable signing key.

## Remaining scope

This is a development build, not physical-device performance/accessibility certification. Full-app snapshots, structural undo, production signing remain separate work. Screenplay templates do not provide professional pagination. Cloud sync, collaboration and AI are not required for the local writing workflow.

Read `PROGRESS.md` and the latest owner amendments in `docs/PRODUCT_REDESIGN.md` before continuing. Older screenshots and design documents may show retired Arrange/Read workflows.

Custom fields are managed in Settings → Writing → Custom fields and edited from Page actions → Custom fields. Reusable structures are saved in Settings → Writing → My templates. They copy names, types, roles, nesting and field definitions, with optional empty documents; manuscript text is never copied. Project backups include field definitions and values, with reference IDs remapped on restore.

## First LTS release candidate

The workspace has Overview, Write, and an owl AI tab marked Coming soon. Overview exposes editable novel details, synopsis, goal and cover import. The app targets API 36. CI builds an unsigned minified release bundle and tests Android 14 and 16. See [release preparation](docs/RELEASE.md); signing and store publication remain owner-controlled.
