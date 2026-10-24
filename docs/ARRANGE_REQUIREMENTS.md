# Arrange: requirements and interaction design

## Purpose and ownership
Write owns chapter creation and prose. Plan owns reference resources. Arrange owns membership and order of existing chapters in the manuscript. Read and manuscript export consume that same order. Files owns folder organization; moving folders never changes manuscript order.

## Behavior
- Arrange shows only included chapters, numbered by current reading order, with title and word count.
- Add chapter opens a searchable picker of existing, active Chapter documents in this novel. It never opens creation. Resources, screenplay files, folders and trashed documents are excluded.
- Already included chapters are visible, marked Added, and cannot be selected again. Duplicate titles are distinguished by folder path; selection uses stable document IDs.
- Multiple selected chapters append in their displayed order in one transaction. Repeated submissions cannot duplicate or reorder already included documents. Cancel changes nothing.
- If there are no chapters, the picker shows “No chapters yet” and Go to Write. If every chapter is included, show “All chapters added”. A failed search shows “No matching chapters”.
- Remove from manuscript replaces the ambiguous “Unplace”. It changes membership only. Text, folder, links and revisions survive; the chapter remains in Write and the picker and can be re-added at the end.
- Move earlier/later changes manuscript order only, with disabled boundary actions. All changes persist immediately.
- Newly created chapters in Write/Files start outside the manuscript. Existing novels, restored backups and standard starter chapters retain their current membership; opening the app never silently rearranges them.

## Screen design
A Manuscript title, chapter count and Add chapter action; compact ordered chapter rows with reorder and removal controls. No instructional paragraphs, unplaced-draft section or creation form. Empty-state messages and action labels are concise. The picker has search, checkboxes, folder paths and Add selected / Cancel actions. Existing bottom navigation remains.

## Architecture and data safety
Reuse domain:manuscript contracts, the existing placement table and stable document IDs. Add a transactional batch operation in LocalManuscriptRepository with workspace/type/deletion validation before any write. WorkspaceViewModel coordinates editor flush, busy state and errors. A separate picker composable collects selection; it does not mutate storage. No schema change, duplicated document, folder rewrite or new dependency.

## Acceptance tests
1. Creating a draft does not add it to Arrange; selecting it does.
2. Batch addition preserves prior order, rejects cross-workspace/non-chapter/deleted IDs atomically, and is idempotent.
3. Removing/re-adding retains body, folder and links and appends at the end.
4. Database reopen and portable backup preserve membership/order.
5. Device flow opens the picker rather than creation, removes and re-adds a chapter, and Read follows the arrangement.
6. Existing template/migration, writing, resource and backup tests continue to pass.
