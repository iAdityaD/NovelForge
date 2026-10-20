# NovelForge architecture (0.3)

NovelForge is a local-first modular monolith: one Android application assembled from independently bounded Gradle modules. Presentation uses MVVM and immutable StateFlow state. Domain contracts and policies are plain Kotlin; data implementations use Room and Android services. Hilt supplies implementations at the application boundary.

## Module ownership

| Module | Owns |
| --- | --- |
| `app` | Application/Activity, Hilt composition, typed destination enum, saved navigation IDs, Android document-picker launchers |
| `core:model` | Storage-independent Book, Entry, EntrySummary, Revision and WritingDay models; text counting/search token helpers |
| `core:database` | Room entities, DAO, database, frozen schema migrations |
| `core:designsystem` | Theme, typography, reusable cards, fields, icons, genre-chip input and display components |
| `core:presentation` | Small ViewModel coroutine/error foundation |
| `domain:workspace` | Workspace repository contract, folder hierarchy and metadata policies |
| `domain:writing` | Writing repository contract and shared editing session with autosave, undo/redo and flush coordination |
| `domain:story` | Story-reference contract and note templates |
| `domain:analytics` | Activity contract and streak calculations |
| `domain:preferences` | Typed preferences state and persistence contract |
| `domain:backup` | Portable backup/restore contract |
| `data:local` | Repository implementations, entity/model mappings, JSON wire codec, document IO and Hilt providers |
| `feature:library` | Library, workspace setup and search ViewModel |
| `feature:workspace` | Explorer, graph, novel details and workspace ViewModel |
| `feature:editor` | Markdown editing and editor ViewModel |
| `feature:reader` | Reader, bookmarks, positions and reader ViewModel |
| `feature:analytics` | Analytics UI and activity ViewModel |
| `feature:guide` | Offline guide lessons |
| `feature:settings` | Category settings, limits, trash and settings ViewModel |
| `feature:backup` | Backup progress/error orchestration |

## Dependency direction

Features depend on domain contracts, shared models, and shared UI infrastructure. They never depend on another feature implementation, Room, or `data:local`. Domain modules never import Android, Compose, Room or UI implementations. Data implements domain contracts. `app` assembles features and data through Hilt.

```mermaid
flowchart TD
    A[App composition] --> F[Feature UI and ViewModels]
    A --> L[Local data implementations]
    F --> D[Domain contracts and policies]
    L --> D
    L --> R[Room database]
```

`scripts/check_architecture.py` enforces these boundaries in CI. Gradle enforces unresolved or circular module dependencies. Keep feature dependencies restricted to the capabilities actually used.

## State and editing

Each stateful feature owns its ViewModel. StateFlow exposes immutable state; Compose collects it with lifecycle awareness. Screens send events to the owning ViewModel. The app shell owns navigation and passes callbacks between features.

`EditorSession` is shared by editor and workspace operations because moving, renaming, deleting or exporting an open document must coordinate with pending edits. It has an injected process-lifetime coroutine scope, serializes saves, and flushes on navigation and Activity stop. Workspace mutations flush first and refresh the open document afterward. Storage rejects writes to trashed documents.

There is still a short 400 ms autosave debounce window. A forced process kill in that window may lose the unsaved fragment. A save failure remains visible and blocks normal editor navigation.

## Persistence and upgrades

Room schema history stays in `app/schemas/com.novelforge.app.data.ForgeDb`. The database class retains its canonical name for migration compatibility; SQL table names are explicit and independent of Kotlin entity names.

Schema 3 adds `BookGenre`, preserving the existing Book/Entry/Revision/FTS data. A legacy genre string becomes one genre value without guessing whether embedded punctuation was a delimiter. All genre writes are transactional with book metadata. New genre lists are trimmed and deduplicated without changing their displayed capitalization.

Domain objects are mapped explicitly to database entities. Backup JSON is a separate versioned wire format, currently v3, with v1/v2 readers. JSON restores remap IDs and internal links transactionally. Existing over-limit synopses are preserved on migration and restore; the editor shows the over-limit state and allows retention or shortening. Global preferences remain device-local and use the existing preference keys.

## Adding a feature

1. Decide which domain owns the behavior. Extend an existing module for a related capability; create a new module only for an independently evolving area.
2. Define storage-independent models, repository operations and meaningful validation policies in that domain. Avoid exposing DAO objects or Android Context.
3. Implement persistence in `data:local`. Add a versioned migration whenever the stored schema changes; preserve old backups.
4. Add a focused ViewModel and immutable UI state to the relevant feature. Inject contracts through its constructor.
5. Reuse the design system and pass navigation callbacks from `app`; avoid direct imports of sibling feature implementations.
6. Cover business rules and migration risks, then exercise the primary user flow. Run the boundary check and CI build.
7. Commit the stage and update `PROGRESS.md` with verified results and remaining work.

Example: character relationships belong to Story. Add relationship models and operations to `domain:story`, normalized storage and migration in `core:database`/`data:local`, and a UI owned by Workspace or a new Story feature. Editor can consume the Story contract for reference suggestions without importing that feature's screen.

## Deliberate scope

The local data implementations currently share one module and one database so cross-domain changes can remain transactional. The graph lives inside the workspace feature. Preferences use a typed abstraction over existing SharedPreferences to preserve installed user settings. Navigation uses typed enum destinations plus saved IDs rather than a new navigation framework. Split these further when a concrete feature requires it, without weakening the domain boundaries.

Production release signing, physical-device performance/accessibility review and the export/media roadmap remain separate work.
