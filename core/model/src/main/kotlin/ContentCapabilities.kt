package com.novelforge.core.model

/** Compatibility boundary until project-owned type definitions replace legacy kind strings. */
private val manuscriptKinds = setOf("Document", "Chapter", "Screenplay")
fun isManuscriptKind(kind: String): Boolean = kind in manuscriptKinds

/** Legacy chapters were implicit members. Other drafts require an explicit placement. */
fun manuscriptIncluded(kind: String, included: Boolean?): Boolean =
    isManuscriptKind(kind) && (included ?: (kind == "Chapter"))
