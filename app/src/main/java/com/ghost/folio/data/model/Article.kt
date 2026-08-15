package com.ghost.folio.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val body: List<BodyBlock>,
    val tags: List<String>,
    val relatedIds: List<String>,
    val lastUpdated: String,
    val difficulty: Difficulty,
    val hasDiagram: Boolean,
    val isSaved: Boolean = false
)

@Serializable
enum class Difficulty {
    @SerialName("BASIC")
    BASIC,
    @SerialName("INTERMEDIATE")
    INTERMEDIATE
}

@Serializable
sealed class BodyBlock {
    @Serializable
    @SerialName("paragraph")
    data class Paragraph(val text: String) : BodyBlock()

    @Serializable
    @SerialName("heading")
    data class Heading(val text: String) : BodyBlock()

    @Serializable
    @SerialName("definition")
    data class Definition(val term: String, val definition: String) : BodyBlock()

    @Serializable
    @SerialName("bulletList")
    data class BulletList(val items: List<String>) : BodyBlock()

    @Serializable
    @SerialName("diagram")
    data class Diagram(val key: String) : BodyBlock()

    @Serializable
    @SerialName("note")
    data class Note(val text: String) : BodyBlock()

    @Serializable
    @SerialName("comparison")
    data class Comparison(
        val headers: List<String>,
        val rows: List<ComparisonRow>
    ) : BodyBlock()
}

@Serializable
data class ComparisonRow(
    val label: String,
    val values: List<String>
)

