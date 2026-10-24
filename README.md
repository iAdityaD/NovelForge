# NovelForge — local Android writing workspace

NovelForge 0.6.0 is a local-first writing app built with Kotlin, Jetpack Compose, Room, Hilt and Coroutines/Flow. Organize research in **Plan** and write/reorder a nested manuscript in **Write**. The workspace has three tabs: **Overview, Plan, Write**. Separate Arrange and Read screens are no longer part of active navigation.

## Current workflow

- **Library:** grid/list projects, search, combined status/genre filters, backup import and creation.
- **Create:** title required; author, multiple genres and color optional. Start blank or use an optional Novel, Short story, Screenplay, Nonfiction or Essay recipe. Generated folders remain editable. Customize Novel starter folders in Settings.
- **Overview:** project identity, editable synopsis, resume writing and manuscript totals. Project metadata uses a separate pencil action. Export, backup and confirmed workspace deletion remain in the menu.
- **Plan:** nested folders and documents for ideas, notes, research and sources. Names and type labels are freeform. Add a category inside the resource form; optional URLs are stored with notes, not downloaded.
- **Write:** one canonical manuscript tree for writing and organization. Every manuscript document participates automatically. Long-press and drag a row above/below another to reorder; edge dragging scrolls. Move up/down and Move to folder remain available.
- **Files:** the whole logical workspace, compact collapsible rows, file search, custom type/status filters, rename/move/trash and scroll position indicators. Filters preserve ancestor folders.
- **Editor:** Markdown, syntax colors, typography, focus mode, stable internal links, backlinks, undo/redo, checkpoints, custom type labels/statuses, word counts and truthful autosave state.
- **Guide:** searchable offline articles matching the current workflow, including resources, writing, nested order, linking and recovery.
- **Analytics:** streak and project selector, all-project portfolio totals or selected-project metrics, manuscript words/pages/documents, resources and activity.
- **Settings:** focused category subpages for Appearance, Editor, Writing, Data & backup, Recently deleted and About. Writing includes default folders and the synopsis word limit. No reader settings are exposed while Read is deferred.

The UI uses charcoal/violet and light themes, compact rows and scrollable forms. Templates are optional starting content, not enforced taxonomies. A document's stored Plan/manuscript role is independent of its editable title and type label.

## Data model and architecture

The existing Android stack is retained. A domain-based modular monolith separates pure Kotlin models/contracts, application services, Room repositories, feature ViewModels and Compose UI. No SQL belongs in UI components. See [architecture](docs/ARCHITECTURE.md) and [the current product specification](docs/PRODUCT_REDESIGN.md).

Room schema **5** adds the explicit document area while retaining IDs, content, references, folders and revisions. Legacy mixed folders can appear as ancestors in both Plan and Write; migration does not silently reorganize or delete their text. Prior manuscript exclusion records remain for historical compatibility, but no longer hide documents from the current manuscript. Nested folder/sibling order is now shared by Write, export and manuscript statistics.

`WorkspaceRepository.createNode` creates generic documents/folders and optional resource categories transactionally. `placeBefore` validates project/area ownership and cycles, then changes parent/sibling order atomically. `orderedManuscript` is the iterative depth-first projection used for canonical manuscript order. Type labels and document statuses remain portable freeform values; this is not yet a schema-based custom metadata/type registry.

## Storage, backup and recovery

Everything works without an account or network connection. The app has no internet permission. The explorer is a logical SQLite tree, not a live external Markdown vault.

Recovery backup **v6** preserves project metadata, documents, custom labels/statuses, Plan/manuscript roles, folder order, trash, checkpoints and writing history. Restore validates bounded input and creates a separate project in one transaction, remapping IDs and internal links. Legacy v1–v5 backups remain supported. Markdown export contains the complete current manuscript in nested order, excluding Plan resources.

Project backups do **not** include app preferences, historical reader bookmarks/positions or pending unsaved drafts. Limits: 16 MB and 10,000 documents per backup. Export before uninstalling or changing devices. Android cloud backup is disabled.

Autosave runs after a short pause and flushes on navigation/backgrounding. Saved locally follows successful persistence. Save failures block normal editor departure. A forced process kill during the unsaved debounce window can still lose the latest fragment; durable draft journaling remains future work.

## Build and installation

Use JDK 17 and Android SDK 35. Android 8.0/API 26 or newer is required.

```sh
python3 scripts/check_architecture.py
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
```

GitHub Actions also runs API 34 emulator journeys and migration checks. Successful builds provide the `NovelForge-debug` APK artifact, test reports and actual device screenshots. Version 0.6.0 source `0c3fb6ccfa2a6c39bb30f8bee7aca3c64e7f50a7` passed 37 JVM tests, seven API 34 Android tests, lint and architecture checks in [CI run 40114860066](https://github.com/iAdityaD/NovelForge/actions/runs/40114860066). APK SHA256: `e70d9a903dabb8f53b0514769797bee09782250eae6d48c5b4f9de5a168143b4`. See [PROGRESS.md](PROGRESS.md) for checkpoints and remaining work.

**Before replacing an older development build, export every project outside app storage.** CI debug signatures may differ. If Android rejects an update signature, do not uninstall until backups are secured. Uninstalling deletes local data. Production distribution requires an owner-controlled stable signing key.

## Remaining scope

This is a development build, not physical-device performance/accessibility certification. Full custom field schemas and reusable user-authored project recipes, durable draft recovery, full-app snapshots, structural undo, media/cover import and production signing remain separate work. Screenplay templates do not provide professional pagination. Cloud sync, collaboration and AI are not required for the local writing workflow.

Read `PROGRESS.md` and the latest owner amendments in `docs/PRODUCT_REDESIGN.md` before continuing. Older screenshots and design documents may show retired Arrange/Read workflows.
