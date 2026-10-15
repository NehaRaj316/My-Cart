package com.example.data.local.converter

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromLongList(list: List<Long>?): String {
        if (list.isNullOrEmpty()) return ""
        return list.joinToString(separator = ",")
    }

    @TypeConverter
    fun toLongList(data: String?): List<Long> {
        if (data.isNullOrBlank()) return emptyList()
        return data.split(",").mapNotNull { it.trim().toLongOrNull() }
    }

    @TypeConverter
    fun fromPairList(list: List<Pair<String, String>>?): String {
        if (list.isNullOrEmpty()) return ""
        return list.joinToString(separator = "\u001F") { "${it.first}\u001E${it.second}" }
    }

    @TypeConverter
    fun toPairList(data: String?): List<Pair<String, String>> {
        if (data.isNullOrBlank()) return emptyList()
        return data.split("\u001F").mapNotNull { entry ->
            val parts = entry.split("\u001E")
            if (parts.size == 2) parts[0] to parts[1] else null
        }
    }
}
