# NovelForge full workspace upgrade plan

Status: proposed implementation roadmap; UI direction approved by the owner. This document does not mark new features as implemented.
Baseline: verified 0.4 source eef9834e796ddb90992882f5b993cdccf29628e2; documentation checkpoint a4711a7841bae019f4887c958fdaa6fcc3ae86c7.

## Product outcome

A local-first Android writing environment combining flexible folders and linked Markdown documents with a clear Plan → Write → Arrange → Read workflow. One document has one canonical body and stable identity, regardless of the view used to access it. Planning is optional. Authors may draft whole chapters or divide chapters into scenes.

The approved generated boards establish charcoal surfaces, restrained violet accents, compact explorer rows, readable typography, separate metadata pencil and overflow, and a warm reading mode. They are visual references, not exact behavioral specifications. Production UI must use consistent icons/type, sufficient contrast, font scaling and keyboard-safe controls. Generated sample word counts are placeholders, not calculation requirements.

## Navigation and screens

Global navigation stays Library, Guide, Analytics, Settings. Opening a novel enters a separate workspace shell with Overview, Plan, Write, Arrange, Read. Preserve selected tab, document, selection and scroll state through navigation/recreation where appropriate. Back closes an open drawer or dialog before leaving the screen; pending changes must be saved or explicitly resolved.

A folder action opens the same explorer on every workspace tab. Phone: modal drawer with scrim and explicit close. Tablet: optional persistent side pane. Focus writing/fullscreen reading hides controls; revealing controls restores explorer access. Selecting a document opens its canonical content in the appropriate editor or reader. Opening planning material from Read must not silently insert it into the manuscript. Remember the prior location for return navigation.

### Overview

Title, optional cover, multiple genres, status, premise/synopsis, Continue writing, recent documents and modest word progress. No dense dashboard. Synopsis is an editable section of the designated overview document; do not maintain a second independently editable copy in Book metadata. Resolve the overview by stable ID, not a folder name. Removing the overview document offers recreation or reassignment without blocking writing. The metadata pencil edits title, author, genres, status, cover and optional goal; overflow contains export, backup and named deletion confirmation.

### Plan

Typed collections for characters, locations, themes, events, props and research. Each collection supports list/search/filter/create and opens ordinary documents with optional structured fields. Relationships and scene references are explicit links. Show linked scenes and backlinks. Category membership comes from type/metadata, not folder names. Start with built-in categories; add user-defined categories/fields after core behavior is verified. Story chronology and manuscript order remain separate.

### Write

Recent drafts, all manuscript documents and unplaced drafts, with New chapter/New scene actions. Focus editor with autosave status, source Markdown, formatting, stable-ID link picker, find/replace, optional scene context and undo/redo. Optional context includes viewpoint, location, story time, subplot and drafting status. Keep planning and revision notes outside compiled prose. Provide a reference sheet on phones and an optional reference pane on tablets. Add revision comparison/restore and document comments without mixing annotations into the manuscript body.

### Arrange

Nested parts, chapters and scenes with independent manuscript order, collapse/expand, drag handles and accessible Move before/after actions. An unplaced tray holds draft documents not yet included. Removing a placement must not delete its source document. Explicitly distinguish Remove from manuscript from Move to trash. Chapters may contain direct prose or scene placements; define serialization order and avoid double-counting if both exist. Prefer a clear direct-chapter mode or scene-based mode, with lossless conversion and preview. Prevent cycles and duplicate placement in the same manuscript by default. Every included item references a stable document ID.

### Read

Preview only included manuscript content in Arrange order. Warm/light/dark themes, typography, contents, remembered position, bookmarks, chapter navigation and hideable controls. Switch from a passage to its source document for revision. Planning guides, archived drafts, comments and optional notes are excluded. Opening an individual reference document uses document reading mode, clearly separate from full-manuscript preview.

### Library, creation, guide and settings

Library retains combined status/genre filter, search, import beside search, grid/list and continue writing. Later support optional series grouping without forcing a series on standalone books.

Creation requires only title. Author, genres and cover/color are optional. Standard structure is preselected; Build from scratch creates no content. Preview starter folders in a collapsed section. More options holds optional target/deadline/status and later series association. Default status In progress; no invented deadline or mandatory word goal. Open Overview with Start writing after creation. For scratch workspaces, Overview is a UI empty state and does not silently create folders/documents; Start writing creates the first document. Standard creation opens its designated overview and provides access to Chapter 1. New chapters should not count template instructions as manuscript words.

Keep settings categories and subpages. Add structure/templates, manuscript defaults, storage/backup policies and goal preferences in their respective categories. Include concise contextual guidance and maintain an offline Guide. Do not force onboarding before drafting.

## Architecture and data ownership

Retain Kotlin, Compose, MVVM/StateFlow, Hilt, Room and local repositories. Domain modules remain plain Kotlin; UI must not import Room or sibling feature implementations. Keep one database for transactional cross-domain changes. Extend modules when related; create modules only for independently evolving responsibilities.

- Workspace: logical folders, document identities, lifecycle, explorer and metadata.
- Writing: editing session, autosave, revisions and annotation operations.
- Story: typed profiles, relationships, scene context and story events.
- Manuscript (new domain): parts/chapters/placements, ordering, inclusion, compilation inputs and manuscript statistics.
- Series (later domain): series/world containers, novel membership and shared-reference ownership.
- Export (new domain when implemented): compilation settings and exporter contracts; Android IO stays outside the domain.
- Backup/Preferences/Analytics: extend existing contracts for complete snapshots, user settings and accurate statistics.
- Presentation: small workspace shell coordinating Overview/Plan/Write/Arrange/Read; add focused planning/arrangement modules rather than expanding one large screen/ViewModel.

Proposed additive records include Manuscript, ManuscriptNode (parent, ordering, optional document reference), DocumentMetadata, DocumentLink, StoryRelationship, StoryEvent, Annotation and later Series/Membership/SharedReference. Final schema design follows a focused migration prototype. Existing Entry IDs are retained; legacy folderId remains physical/logical organization only. Avoid overloading legacy parentId or position with a second meaning.

The manuscript compiler produces an ordered stream used by Read, export and manuscript counts. Do not copy scene bodies into chapter records. Backlinks can be indexed from canonical text and rebuilt; explicit relationship metadata remains authoritative. Design annotation anchors to detect stale text after edits rather than silently attaching comments to the wrong passage.

## Delivery stages and gates

### Stage 1 — Safety and migration foundation

Set up stable signing for future updates using owner-controlled credentials stored outside source control. Never commit signing secrets. The existing CI debug certificate cannot be assumed recoverable; document a one-time backup/restore path if signatures cannot match. Extend backup to full snapshots including revisions, trash, links, activity, settings and later attachments; keep portable per-novel export as a separate option. Validate snapshot/restore integrity, version compatibility, bounded parsing and transactional restoration. Add migrations without destructive fallback. Gate: restore representative old backups and databases, preserve all IDs/content, show controlled errors for corrupt files, and test upgrade continuity with the stable signing configuration when credentials are provisioned.

### Stage 2 — Workspace shell and approved design system

Implement five workspace tabs, persistent navigation state, universal explorer, Overview and streamlined creation. Retain current functionality behind the new routes while new views arrive. Gate: empty and populated workspaces, Back behavior, keyboard-open forms, large text, light/dark themes and small screens verified. No dead placeholder navigation in a delivered build.

### Stage 3 — Manuscript domain and Arrange

Introduce manuscript nodes and placement operations, unplaced tray, direct-chapter support, scene grouping and independent ordering. Migrate existing chapters in their established order without rewriting prose. Provide a reviewable preview when old hierarchy is ambiguous. Gate: reorder/move/remove/undo/restart; no body duplication, lost references or count inflation; legacy novels compile consistently.

### Stage 4 — Story planning views

Add typed collections, profiles, optional metadata, explicit relationships, linked scenes, story timeline and reference access from writing. Preserve arbitrary folders. Gate: profile edits appear in explorer and Plan; rename/move keeps references; events retain chronology without altering reader order; deleted links show recoverable missing-target states.

### Stage 5 — Writing and revision

Polish editor, scene context, find/replace, source navigation, focus behavior, checkpoint comparison and comments. Persist optional notes independently from compiled text. Harden save ordering and failure feedback; test forced termination around pending writes and document any remaining durability window. Gate: meaningful long-text editing, undo/redo, replacement preview, save failure recovery, restore and concurrent document operations.

### Stage 6 — Reading and compilation

Use the shared compiler for whole-book reading; add contents and source navigation. Deliver Markdown, EPUB and DOCX export with title/author, ordered chapters, cover when supplied and deterministic exclusion of planning material. PDF follows with separately validated pagination and fonts; screenplay pagination is a separate capability. Gate: compare reader/export ordering and content, test Unicode, empty chapters, special characters, long books and broken/missing attachment references; open generated files in independent readers.

### Stage 7 — Shared worlds and series

Add optional series/world container above independent novel workspaces. Shared character/location documents live once in the container; novels reference them. Define novel-specific notes without mutating shared canonical facts. Moving a standalone novel into/out of a series must have explicit copy/reference semantics. Block or resolve deletion of referenced shared material. Gate: cross-book links, portable export dependency inclusion, full-series restore and safe detachment verified.

### Stage 8 — Goals, analytics and release hardening

Add optional deadlines, daily targets, configurable reminders and scheduled backups using supported Android scheduling APIs. Analytics separates manuscript, planning and total stored words; track portfolio, series and novel scope. Exclude default guide/template text from authored manuscript progress. Add optimized cover/media handling, retention controls and storage breakdown. Finish accessibility, tablet layouts and physical-device performance checks, then prepare production packaging.

Each stage: implementation commit, focused tests and required CI, visual inspection on relevant screens, working APK when a stage is user-usable, and PROGRESS.md checkpoint recording source SHA/results/open risks. No claim of completion based only on mockups or successful compilation.

## Storage, performance and reliability criteria

Use indexed summary queries and paged/lazy lists; load bodies on demand. Incrementally cache counts/link indexes and rebuild safely when needed. Store attachments once using content hashes with bounded thumbnails; backups include a manifest and integrity checks. Delete media only when no active/recoverable references remain, respecting trash retention.

Define representative fixtures: small novel, 150k-word novel, 500k-word manuscript, and large shared-reference collection. Benchmark editor latency, explorer scrolling, save time, search, reader navigation and export memory on a named physical mid-range Android device before making speed claims. Initial release targets: 60fps scrolling where supported, no routine main-thread IO, no ANRs in tested flows, and bounded memory during large exports. Record actual measurements and refine budgets from the baseline.

Release gates include TalkBack labels/focus, accessible movement alternatives, at least 48dp actions, font scaling, contrast, rotation/process recreation, no-storage/network cases for backup destinations, interruption-safe operations and migration/restore regression tests. Offline writing never requires an account or network.

## Scope boundaries

Core release: safe updates/backups; complete single-novel workspace workflow; Plan/Write/Arrange/Read sharing canonical content; modern accessible UI; reliable manuscript export. Full roadmap additionally includes series, attachments, goals and scheduled backup. Cloud collaboration, automatic story generation, a plugin ecosystem, general Obsidian-vault sync and professional screenplay layout are separate future projects and must not delay the core.

Open implementation dependencies: owner-held release signing credentials; access to physical devices for measured performance certification; decisions on any paid distribution or external sync provider, only if introduced later. These do not block local-first UI/domain development.
