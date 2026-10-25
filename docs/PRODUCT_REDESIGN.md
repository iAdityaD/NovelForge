# Current milestone override — 25 October 2026

This section supersedes navigation and file-placement requirements in the historical specification below.

- Workspace tabs: Overview, Write, AI. AI uses an owl icon and only displays Coming soon. Plan, Arrange and Read are not tabs.
- Overview: local cover import/removal, title, author, multi-genre, status, synopsis, goal and totals, with editable details and scrolling.
- Write: all workspace content, location dropdown, nested folders, separate new-file/new-folder buttons, filename search and type/status filters. Creation exposes manuscript/reference purpose; naming and folder moves do not change purpose.
- Long-press dragging uses before/after row edges and the middle of a folder for nesting. Move dialog excludes self/descendant destinations; domain validation still rejects cycles and cross-project moves. Duplicate remaps references inside copied subtrees.
- Existing projects keep IDs, links, roles, text and ordering. Backup v8 adds normalized cover images; database schema 7 upgrades from schema 6 without destructive recreation.
- Android 16 target, unsigned release-bundle CI and signing configuration prepare a release candidate. The LTS label requires verified results and the owner's production signing, release testing and publication decision.

---

# NovelForge: configurable writing workspace

Status: implementation specification with a newer owner workflow amendment below; broader customization/reliability work remains incomplete.

## Final navigation amendment — 2026-10-24 (takes precedence)

The owner removed Arrange as a separate tab. Workspace navigation is **Overview, Plan, Write**. Write combines the editable manuscript outline and all drag/move/order controls. Read remains deferred. References to a separate Arrange tab below are superseded.

## Owner amendment — 2026-10-24

- Plan holds research, notes, sources, ideas and freely named resource categories as nested folders/documents.
- Write is the nested manuscript. Arrange is a second view of this same hierarchy and sibling order, including every manuscript document. The earlier independent placement and inclusion rules below are superseded.
- Read is removed from active navigation for now. Existing reader code/settings may remain dormant for future work; no Read tab or editor shortcut is exposed.
- Persist role independently from names and editable type labels. Names never determine behavior for new nodes. Legacy kind inference is restricted to migration/import compatibility.
- Drag before/after reorders the canonical tree. Move-to-folder and up/down remain accessible alternatives. Source IDs, text and links remain unchanged.
- Forms and lists scroll; file filters use project-owned labels/statuses. Guidance lives in Guide instead of permanent instructional paragraphs.
- Existing folder organization is preserved during migration. Legacy mixed folders can appear as ancestors in both projections. All old manuscript drafts become visible in Arrange/export, including formerly excluded drafts, as requested by the new workflow.

The detailed earlier sections remain design history where they conflict with this amendment.
Decision date: 2026-10-24. Supersedes conflicting product rules in WORKSPACE_UPGRADE_PLAN.md. Existing version 0.5.2 remains the verified application baseline.

## 1. Scope and technology decision

Keep Android, Kotlin, Jetpack Compose, Room/SQLite, Hilt, Coroutines/Flow and feature-based MVVM. Do not introduce React Native, Expo, a cloud backend or a second application. Retain the existing repository history, domain boundaries and local-first behavior. This decision does not promise iOS support from the Android codebase.

Redesign the product around projects, canonical documents, containers, resources, links, type definitions, reusable templates and views. Novel, short story, screenplay, nonfiction and essay are template configurations rather than separate applications. Blank projects impose no folder structure or story taxonomy.

## 2. Current-source audit

| Finding | Evidence | Required resolution |
| --- | --- | --- |
| Writing and compilation disagree | WorkspaceStudio accepts Chapter/Screenplay; manuscriptChapters, ChapterPicker and LocalBackupRepository filter Chapter | One type-capability resolver and manuscript projection across every consumer |
| Plan category and storage location are conflated | ResourceDialog selects folders; WorkspaceStudio groups references by folder | Resource type is semantic; folder is organizational. Preserve existing folders and membership during migration |
| Fixed novel metadata | Book exposes genre, author, synopsis, target; creation and overview text assume a novel | Optional project fields configured by template/type; preserve legacy fields in migration |
| Closed type list | Templates.kinds and BackupCodec validate against fixed names | Versioned project-owned type registry, stable type IDs and validated custom definitions |
| Flat arrangement | ManuscriptPlacement stores documentId, position and included only | Manuscript node hierarchy with independent parent/order, source document references and reversible structural operations |
| UI coordinates too many responsibilities | WorkspaceStudio owns five views and multiple dialogs; ForgeApp coordinates ad-hoc destination state | Focused screen components and ViewModels; explicit saved navigation state and editor return destination |
| Partial backups | LocalBackupRepository serializes active entries and placement; omits revisions/trash/settings/activity | Full snapshot format and restoration gate before data-model migration |
| Pending edits are volatile | EditorSession keeps pending text in memory with 400 ms debounce | Separate committed revision and recoverable draft journal; save/error state remains truthful |
| Content format is implicit | Entry.body is Markdown without a document-format envelope | Versioned content adapter retaining legacy Markdown losslessly; structured editor selected only after a native spike |
| Synopsis can be duplicated manually | Book.synopsis and generated overview profile both contain synopsis text | Project synopsis is authoritative metadata; generated overview must not maintain a second editable metadata copy |
| Production quality is unmeasured | Existing automated tests cover selected flows, not physical-device performance or complete accessibility | Named device/fixture matrix; measured editor and scrolling performance; TalkBack/large-text review |

This is a source review. It does not certify untested behavior or imply every possible issue has been found.

## 3. Definitive product rules

### Project
A complete writing workspace. Title is required; author, genres (multiple), summary, status, word target and other metadata are optional. Project-owned settings and type definitions are copied from the selected template. Updating a template never changes an existing project. Current Book IDs are retained as project identities.

### Canonical document and container
A document has one stable ID and one authoritative content payload. A container groups nodes. Files exposes the organizational tree; ordinary type definitions may allow children, with explicit cycle rules. Resource is a document role, not a duplicated body in a separate subsystem. Existing Entry IDs and folder relationships must survive migration.

### Type definitions
Each project owns definitions with stable ID, display name, role, content format, allowed children, manuscript eligibility and optional metadata field schema. Rename changes display labels, never identity. Do not infer semantics from a folder name or display label. Built-in Character, Chapter, Scene and Research are editable starting configurations. Users can define their own types and fields. Types in use cannot be removed without an explicit reassignment preview; no silent conversion or deletion.

Field definitions support text, multiline text, number, date, boolean, single/multiple choice, URL and document reference. Unknown fields from supported imports remain preserved even when the UI cannot edit them. Optional means optional: no planning field blocks writing.

### Organization versus manuscript
Preserve the owner's established independence between Files and Arrange. One document may live in a research/drafts folder while appearing elsewhere in manuscript order. A separate manuscript node tree references canonical IDs. Arrange and Read share this manuscript tree and compiler; they do not own copies of prose. File moves do not reorder the manuscript.

A manuscript group may be a Part, Act or user-named container. A leaf references one eligible document. A document cannot appear twice in one manuscript. Groups have no copied document body. A chapter with scenes uses a group containing ordered scene references; converting an existing prose chapter retains its prose as a child source document and presents a preview. Never silently concatenate or discard text.

### Links and relationships
Inline references contain stable target IDs. Their backlink/search index is derived from committed content and can be rebuilt. Authored relationships (e.g. sibling, located-in, research-for) are separate authoritative records. Renaming/moving preserves both. Broken references remain visible and recoverable; do not silently relink duplicate titles. Linking a resource never adds it to the manuscript.

### Content and editor
Existing Markdown remains readable/editable without lossy conversion. Define an editor/content adapter with load, change, flush, selection, undo, reference insertion and serialization operations. Add a versioned content envelope before introducing other formats. Do not replace reliable plain-text behavior with an unverified rich editor. Screenplay is eligible content, but professional pagination is a distinct future capability.

### Lifecycle
Archive, remove from manuscript and move to trash are distinct operations. Archive hides active lists but preserves references. Manuscript removal changes placement only. Trash is recoverable. Permanent deletion identifies affected documents and references and requires explicit confirmation. Structural operations have Undo backed by reversible persisted commands, not an in-memory UI-only undo stack.

## 4. Complete screen and interaction design

Global tabs remain Library, Guide, Analytics, Settings. Project tabs remain Overview, Plan, Write, Arrange, Read. Global tabs disappear inside the project. Editing uses a focused full-screen route with no persistent project tab bar. Files is available from every project view and editor/reader. Back closes keyboard/sheet/drawer before navigation; editor departure flushes changes or exposes a recoverable failure.

| Screen | Primary content | Primary action | Secondary actions |
| --- | --- | --- | --- |
| Library | Project list/grid, title, cover, status, writing progress | New project | Search, filter, import in top bar; project actions menu |
| Create | Project title and template cards: Blank, Novel, Short story, Screenplay, Nonfiction, Essay, My templates | Create project | Optional fields; expandable structure preview |
| Overview | Project identity, readable synopsis, Resume writing, restrained progress | Resume / Start writing | Pencil per editable section; project menu; Files |
| Plan | Searchable resource list; category chips/list only for categories that exist in this project | + Resource | Type management, metadata filters, backlinks/relationships within a resource |
| Add resource | Name, existing/new resource type, optional location and metadata | Create resource | External source type with URL/citation/notes; no article download implied |
| Write | Canonical drafts, folders/groups, optional type/status filters | + Document | Rename, move, duplicate, archive; manuscript membership badge |
| Editor | Title and writing canvas; compact formatting toolbar above keyboard; truthful save status | Write | Link autocomplete, reference sheet, focus mode, find, revisions |
| Arrange | Collapsible manuscript tree with numbered rows and drag handles | + Add existing | Group creation, move before/after/into, remove, Undo, numbering options |
| Add existing | Searchable eligible documents with folder/type detail; existing placements marked Added | Add selected | Cancel; Go to Write only for empty source list |
| Read | Compiled manuscript and table of contents, no planning material | Read / Resume | Typography, contents, bookmarks, edit source, exit |
| Settings | Short category index | Open a category | Appearance, Writing, Reading, Types & Templates, Data & Recovery, Accessibility, About |

### Concrete journeys
1. Blank project → title → empty Overview → Write → create a user-named document type or use generic Document → write → Arrange → select source → Read. No novel folders or character fields are injected.
2. Novel template → preview optional structure → create → draft a chapter → type [[ → select/create a resource → return to the same cursor → add chapter to manuscript → read compiled order.
3. Screenplay template → Acts/Scenes configuration → write scenes through the same editor interface → arrange scenes in acts → read/export ordered scenes. No Chapter-only condition may exclude them.
4. Nonfiction template → Sections and Research → optional citations → sources stay in Plan; only selected sections compile.
5. Rename/move a linked resource → ID remains constant → rendered label resolves from target → backlinks and manuscript membership remain unchanged.
6. Add a custom type → choose role/capabilities/optional fields → immediately usable in creation and filters → preserved in backup and restart.

### Interaction states
- Empty: concise state and one meaningful action, no tutorial paragraphs.
- Loading: preserve last available content where safe; disable only conflicting mutations.
- Failed save: visible status and Retry; retain draft and block unsafe navigation.
- Picker cancellation: no folder, document or placement created as a side effect.
- Keyboard: form actions reachable; editor cursor visible; return restores selection/scroll.
- Duplicate labels: show type and full folder path; every operation uses IDs.
- Long titles/large text: wrapping or ellipsis with accessible full labels; no fixed-height clipping.
- Deleted selection during a picker operation: validate in the transaction and show a controlled error with no partial addition.

## 5. Visual system

Keep charcoal/violet identity but replace inconsistent per-screen styling with shared semantic tokens. Starting palette: background #17171C, surface #24242C, elevated #2D2D36, primary text #E8E5ED, secondary text #A5A2AE, accent #B99AF7, border #383842. Verify contrast for each actual foreground/background combination; these values alone are not an accessibility guarantee.

Spacing scale: 4/8/12/16/24/32 dp. Minimum interactive target: 48 dp. Standard list rows, consistent header and sheet layouts; icons from the existing Material set. Use cards only when they represent an independent object or choice, not to wrap every section. Body typography follows platform font scaling; editor/reader typography is separately configurable.

No permanent helper paragraphs under tab titles. Put guidance in Guide, explicit help actions, empty states or validation. Menus use consistent verbs. Inline field errors stay near the field. Destructive actions are separated from ordinary actions. Reduced-motion/system preferences should be respected. Insets are owned once by the shell; nested views consume them. Device screenshots, not generated imagery, determine implementation acceptance.

## 6. Kotlin module and data design

Preserve existing Gradle boundaries. Introduce responsibilities only when implemented:
- core:model: stable IDs, document roles, type/field definitions, versioned content descriptors and immutable state models.
- domain:workspace: project/document lifecycle and organizational tree rules.
- domain:templates: validated declarative definitions and project instantiation; no Compose/Room imports.
- domain:writing: editor adapter contract, draft/save/recovery orchestration.
- domain:story: inline-reference index contract and authored relationships.
- domain:manuscript: placement tree, transactional structural commands and canonical compilation projection.
- domain:backup: portable project versus full-snapshot contracts and version policies.
- data:local/core:database: Room entities, migrations, indexes, transactions and repository implementations.
- feature modules: focused ViewModels/screens; app shell owns routes and navigation only.

Proposed additive records: ProjectTypeDefinition, MetadataFieldDefinition, DocumentMetadata, ManuscriptNode, StoryRelationship, RecoveryDraft and StructuralCommand. A versioned project template snapshot is stored independently of the reusable template. Exact schema and indexes must be validated in migration tests before being treated as final.

The compiler returns ordered source references with heading/numbering metadata. Reader, export and manuscript analytics consume this one projection. Optional planning notes are stored separately from compiled prose. No feature duplicates eligibility or sorting rules.

## 7. Migration and safety gates

1. Implement full snapshot export/restore covering active content, trash, revisions, placements, types, metadata, links, activity and preferences. Validate checksums, bounded input and transactional restore before relying on it for migration.
2. Preserve Book/Entry identities. Add explicit fields/tables rather than reinterpreting folderId, parentId or position. Map legacy kinds to project-owned definitions deterministically.
3. Keep existing resource folders. Infer legacy resource type only from recorded kind; never guess from folder titles. Generic Resource remains generic unless the user changes it.
4. Import legacy backups through explicit version adapters. Unknown newer versions fail before writes. Backup v4 cannot encode the future registry/tree completely; introduce a new format when those records land.
5. Migrate flat placements to root manuscript nodes, preserving included/excluded status and order. No extra auto-inclusion during migration.
6. Keep legacy Markdown bytes and test stable-ID references after migration. Structured conversion is opt-in and reversible.
7. Keep the current install usable until migration/restore gates pass. No destructive migration fallback.
8. Stable production signing requires an owner-controlled key outside source control. Do not assume existing CI debug signing can be reproduced. Account for this separately from database compatibility.

## 8. Implementation sequence and exit criteria

### A — Audit/design checkpoint (this document)
Resolve conflicts between prior plans and the new specification. Record current-source gaps and exact ownership rules. No app behavior or schema change is claimed by this checkpoint.

### B — Recovery and migration foundation
Full snapshots and restoration tests, draft-journal prototype, additive type registry migration and legacy adapters. Gate: representative installed databases/backups retain all content, IDs and placements; malformed inputs have no partial effects.

### C — Configurable projects/types/templates
Generic creation flow, built-in configurations and Blank, project-owned type/field definitions, reusable custom template saving. Gate: create novel, screenplay, short story, nonfiction and custom projects through the same service; restart preserves configuration.

### D — Unified document and resource workflows
Plan/Write projections based on type capabilities, metadata forms, stable linking, focused editor route and reference sheet. Gate: custom types work without application code changes; rename/move/restore preserves links; reliable autosave/error/recovery verified.

### E — Manuscript hierarchy and compilation
Nested groups/placements, drag and accessible movement, Undo, shared compiler for Read/export/analytics. Gate: mixed eligible types compile once in intended order; resource links do not leak into prose; no duplication or lost source content.

### F — Complete visual redesign and hardening
Apply reviewed design-system components across all screens; keyboard/rotation/insets/large-font/TalkBack checks, realistic long-document performance, full journey regressions and export inspection. Gate: documented measurements and named-device results; no claim of physical-device validation from emulator screenshots.

Commit each tested stage and maintain PROGRESS.md. Release only verified user-usable increments; do not label the overall redesign complete because the shell compiles.

## 9. Acceptance matrix

- Built-in and custom templates use the same model and instantiation rules.
- Blank projects remain blank until an explicit user action.
- Resource type, folder location and manuscript placement are independent.
- Manuscript eligibility uses capabilities, never a Chapter string comparison.
- Files and manuscript trees reject cycles and cross-project parent references.
- Rename/move preserves identity, links and metadata.
- Every persisted relationship has defined authority and restore behavior.
- Recoverable drafts survive tested process termination; Saved follows a committed write.
- Reorder/remove/undo survives restart and never rewrites document text.
- Read/export/analytics consume the same compiled selection/order.
- Backups include every promised record and are tested by restoring into a fresh database.
- UI works at 320 dp width and enlarged font sizes, with gesture and three-button navigation and keyboard visible.
- Representative fixtures: empty project, 150k-word novel, 500k-word manuscript, 2,000 resources. Record device, software version, timings and memory; do not invent performance results.

## 10. Explicit exclusions from this redesign's initial release

Cloud synchronization, collaboration, AI, an Obsidian live-vault adapter, professional screenplay pagination and iOS are not prerequisites for this Android local-first release. Rich structured editing is gated by the editor spike; existing Markdown support is not discarded to satisfy a format preference.
