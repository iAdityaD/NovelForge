package com.novelforge.core.model

/** Structure-only template: never stores the author's prose or references to source IDs. */
data class TemplateNode(val key: String, val parentKey: String?, val title: String, val kind: String, val area: String, val position: Int)
data class SavedTemplate(val id: String, val name: String, val nodes: List<TemplateNode>, val version: Int = 1)

fun validateSavedTemplate(template: SavedTemplate): SavedTemplate {
    require(template.version == 1 && template.id.isNotBlank()) { "Unsupported template." }
    require(template.name.isNotBlank() && template.name.length <= 80) { "Use a template name of 1–80 characters." }
    require(template.nodes.size <= 2000) { "A template supports up to 2,000 items." }
    val byKey = template.nodes.associateBy { it.key }
    require(byKey.size == template.nodes.size) { "Template item keys must be unique." }
    template.nodes.forEach { node ->
        require(node.key.isNotBlank() && node.title.isNotBlank() && node.title.length <= 200)
        require(node.kind.isNotBlank() && node.kind.length <= 80 && node.area in listOf(PLAN_AREA, MANUSCRIPT_AREA))
        var parent = node.parentKey
        val seen = mutableSetOf(node.key)
        while (parent != null) {
            require(seen.add(parent)) { "Template folders cannot contain a cycle." }
            val ancestor = requireNotNull(byKey[parent]) { "Template parent is missing." }
            require(ancestor.kind == "Folder") { "Template parent must be a folder." }
            parent = ancestor.parentKey
        }
    }
    return template.copy(name = template.name.trim())
}

fun templateFromStructure(id: String, name: String, entries: List<EntrySummary>, includeDocuments: Boolean): SavedTemplate {
    val nodes = entries.filter { includeDocuments || it.kind == "Folder" }
    return validateSavedTemplate(SavedTemplate(id, name, nodes.map {
        TemplateNode(it.id.toString(), it.folderId?.toString(), it.title, it.kind, it.area, it.position)
    }))
}
