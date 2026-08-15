package com.ghost.folio

import com.ghost.folio.data.model.Article
import com.ghost.folio.data.model.BodyBlock
import com.ghost.folio.data.model.Difficulty
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Test
    fun testArticleSerialization() {
        val article = Article(
            id = "test-article",
            title = "Test Title",
            category = "display",
            summary = "This is a summary.",
            body = listOf(
                BodyBlock.Definition("Term", "Definition text"),
                BodyBlock.Paragraph("Paragraph text"),
                BodyBlock.Diagram("pixel_subpixel"),
                BodyBlock.Note("Note text")
            ),
            tags = listOf("display", "test"),
            relatedIds = listOf("other-article"),
            lastUpdated = "2024-01-01",
            difficulty = Difficulty.BASIC,
            hasDiagram = true
        )

        val serialized = json.encodeToString(article)
        val deserialized: Article = json.decodeFromString(serialized)

        assertEquals("test-article", deserialized.id)
        assertEquals(4, deserialized.body.size)
        assertTrue(deserialized.body[0] is BodyBlock.Definition)
        assertTrue(deserialized.body[2] is BodyBlock.Diagram)
    }

    @Test
    fun testBodyBlockSubtypes() {
        val blocks: List<BodyBlock> = listOf(
            BodyBlock.Paragraph("Para"),
            BodyBlock.Heading("Heading"),
            BodyBlock.BulletList(listOf("Item 1", "Item 2")),
            BodyBlock.Note("Note content")
        )

        val encoded = json.encodeToString(blocks)
        val decoded: List<BodyBlock> = json.decodeFromString(encoded)

        assertEquals(4, decoded.size)
        assertNotNull(decoded)
    }
}
