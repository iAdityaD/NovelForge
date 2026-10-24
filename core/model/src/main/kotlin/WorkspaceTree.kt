package com.novelforge.core.model

const val PLAN_AREA = "plan"
const val MANUSCRIPT_AREA = "manuscript"

/** Include ancestors of matching documents without changing legacy mixed-folder ownership. */
fun areaEntries(entries: List<EntrySummary>, area: String): List<EntrySummary> {
    val byId = entries.associateBy { it.id }
    val ids = entries.filter { it.area == area }.map { it.id }.toMutableSet()
    ids.toList().forEach { id ->
        var parent = byId[id]?.folderId
        val seen = mutableSetOf<Long>()
        while (parent != null && seen.add(parent)) {
            if (byId[parent]?.kind != "Folder") break
            ids.add(parent)
            parent = byId[parent]?.folderId
        }
    }
    return entries.filter { it.id in ids }
}

/** Deterministic depth-first document order shared by Write, Arrange, counts and export. */
fun orderedManuscript(entries: List<EntrySummary>): List<EntrySummary> {
    val nodes = areaEntries(entries, MANUSCRIPT_AREA)
    val children = nodes.groupBy { it.folderId }
    val seen = mutableSetOf<Long>()
    val result = mutableListOf<EntrySummary>()
    fun visit(parent: Long?) {
        children[parent].orEmpty().sortedWith(compareBy<EntrySummary> { it.position }.thenBy { it.id }).forEach { node ->
            if (seen.add(node.id)) {
                if (node.kind == "Folder") visit(node.id)
                else if (node.area == MANUSCRIPT_AREA) result.add(node)
            }
        }
    }
    visit(null)
    return result
}
