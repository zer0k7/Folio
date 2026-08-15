package com.ghost.folio

import com.ghost.folio.data.model.Article
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SeedJsonValidationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Test
    fun testSeedFilesAreValid() {
        val seedDir = File("src/main/assets/seed")
        if (!seedDir.exists()) {
            return
        }

        val jsonFiles = seedDir.listFiles { file -> file.extension == "json" } ?: emptyArray()
        assertTrue("Seed files should exist", jsonFiles.isNotEmpty())

        for (file in jsonFiles) {
            val content = file.readText()
            val articles: List<Article> = json.decodeFromString(content)
            assertTrue("File ${file.name} should contain articles", articles.isNotEmpty())
            for (article in articles) {
                assertTrue("Article ${article.id} must have >= 4 body blocks", article.body.size >= 4)
            }
        }
    }
}
