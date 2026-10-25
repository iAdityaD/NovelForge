package com.novelforge.data.local

import com.novelforge.core.model.*
import org.json.JSONArray
import org.json.JSONObject

object FieldCodec {
    fun encodeDefinitions(fields: List<FieldDefinition>): String = JSONArray().apply { fields.forEach { f -> put(JSONObject().apply {
        put("id", f.id); put("name", f.name); put("type", f.type.name); put("options", JSONArray(f.options))
    }) } }.toString()
    fun definitions(raw: String): List<FieldDefinition> {
        val array = JSONArray(raw)
        val fields = (0 until array.length()).map { i -> val f = array.getJSONObject(i); val options = f.getJSONArray("options")
            FieldDefinition(f.getString("id"), f.getString("name"), FieldType.valueOf(f.getString("type")), (0 until options.length()).map { options.getString(it) })
        }
        validateFields(fields)
        return fields
    }
    fun encodeValues(values: Map<String, List<String>>): String = JSONObject().apply { values.forEach { (key, value) -> put(key, JSONArray(value)) } }.toString()
    fun values(raw: String): Map<String, List<String>> {
        val obj = JSONObject(raw)
        require(obj.length() <= 500) { "Too many stored field values." }
        return obj.keys().asSequence().associateWith { key ->
            val array = obj.getJSONArray(key)
            require(array.length() <= 100)
            (0 until array.length()).map { array.getString(it).also { value -> require(value.length <= 20000) } }
        }
    }
    fun remap(values: Map<String, List<String>>, fields: List<FieldDefinition>, ids: Map<Long, Long>): Map<String, List<String>> {
        val references = fields.filter { it.type == FieldType.REFERENCE }.map { it.id }.toSet()
        return values.mapValues { (id, value) -> if (id in references) value.map { old -> ids[old.toLong()]?.toString() ?: old } else value }
    }
}
