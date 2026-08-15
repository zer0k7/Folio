package com.ghost.folio.data.local.converter

import androidx.room.TypeConverter
import com.ghost.folio.data.model.BodyBlock
import com.ghost.folio.data.model.RelatedLink
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromBodyBlockList(blocks: List<BodyBlock>): String {
        return json.encodeToString(blocks)
    }

    @TypeConverter
    fun toBodyBlockList(jsonStr: String): List<BodyBlock> {
        return try {
            json.decodeFromString(jsonStr)
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStringList(strings: List<String>): String {
        return json.encodeToString(strings)
    }

    @TypeConverter
    fun toStringList(jsonStr: String): List<String> {
        return try {
            json.decodeFromString(jsonStr)
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromRelatedLinkList(links: List<RelatedLink>): String {
        return json.encodeToString(links)
    }

    @TypeConverter
    fun toRelatedLinkList(jsonStr: String): List<RelatedLink> {
        return try {
            json.decodeFromString(jsonStr)
        } catch (_: Exception) {
            emptyList()
        }
    }
}

