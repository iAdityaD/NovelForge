package com.novelforge.core.model

import java.net.URI
import java.time.LocalDate

enum class FieldType { TEXT, MULTILINE, NUMBER, DATE, BOOLEAN, CHOICE, MULTI_CHOICE, URL, REFERENCE }
data class FieldDefinition(val id: String, val name: String, val type: FieldType, val options: List<String> = emptyList())

fun validateFields(fields: List<FieldDefinition>) {
    require(fields.size <= 100 && fields.map { it.id }.distinct().size == fields.size) { "Use up to 100 unique fields." }
    fields.forEach {
        require(it.id.isNotBlank() && it.name.isNotBlank() && it.name.length <= 80) { "Use field names of 1–80 characters." }
        require(it.options.size <= 100 && it.options.all { value -> value.isNotBlank() && value.length <= 200 } && it.options.distinct().size == it.options.size) { "Field choices must be unique and nonblank." }
        if (it.type in listOf(FieldType.CHOICE, FieldType.MULTI_CHOICE)) require(it.options.isNotEmpty()) { "Add at least one choice." }
    }
}

fun validateFieldValue(field: FieldDefinition, values: List<String>) {
    if (values.isEmpty()) return // Every field is optional.
    require(values.size <= 100 && values.all { it.length <= 20000 }) { "Field value is too large." }
    if (field.type != FieldType.MULTI_CHOICE) require(values.size == 1)
    val value = values.first()
    when (field.type) {
        FieldType.NUMBER -> require(value.toBigDecimalOrNull() != null) { "Enter a valid number." }
        FieldType.DATE -> require(runCatching { LocalDate.parse(value).toString() == value }.getOrDefault(false)) { "Use YYYY-MM-DD." }
        FieldType.BOOLEAN -> require(value in listOf("true", "false"))
        FieldType.CHOICE, FieldType.MULTI_CHOICE -> require(values.distinct().size == values.size && values.all { it in field.options }) { "Choose an available option." }
        FieldType.URL -> require(runCatching { URI(value).let { it.scheme?.lowercase() in listOf("https", "http") && !it.host.isNullOrBlank() && it.userInfo == null } }.getOrDefault(false)) { "Enter a valid http or https URL." }
        FieldType.REFERENCE -> require(value.toLongOrNull()?.let { it > 0 } == true) { "Choose a document." }
        else -> Unit
    }
}
