package com.ghost.folio.data.changelog

enum class ChangeType {
    NEW,
    FIX,
    IMPROVED,
    REMOVED
}

data class ChangelogItem(
    val type: ChangeType,
    val description: String
)

data class ChangelogRelease(
    val version: String,
    val date: String,
    val changes: List<ChangelogItem>
)

object ChangelogData {
    val releases = listOf(
        ChangelogRelease(
            version = "1.1.0",
            date = "15 Aug 2026",
            changes = listOf(
                ChangelogItem(ChangeType.NEW, "Settings screen with theme selection, update frequency, reading history, and cache controls"),
                ChangelogItem(ChangeType.NEW, "What's new changelog bottom sheet"),
                ChangelogItem(ChangeType.NEW, "Daily Term home screen widget with midnight WorkManager scheduling"),
                ChangelogItem(ChangeType.NEW, "Text size control with live preview and 5 scale stops"),
                ChangelogItem(ChangeType.NEW, "Long press to copy definitions, paragraphs, and lists with micro-animations"),
                ChangelogItem(ChangeType.NEW, "External References section on article screens linking to trusted documentation"),
                ChangelogItem(ChangeType.IMPROVED, "Dynamic light and dark theme switching"),
                ChangelogItem(ChangeType.IMPROVED, "Enhanced article body typography sizes and spacing")
            )
        ),
        ChangelogRelease(
            version = "1.0.0",
            date = "15 Aug 2026",
            changes = listOf(
                ChangelogItem(ChangeType.NEW, "Initial release"),
                ChangelogItem(ChangeType.NEW, "200+ articles across 20 categories"),
                ChangelogItem(ChangeType.NEW, "Full offline support"),
                ChangelogItem(ChangeType.NEW, "Dark and light theme"),
                ChangelogItem(ChangeType.NEW, "Article bookmarks"),
                ChangelogItem(ChangeType.NEW, "Full text search"),
                ChangelogItem(ChangeType.NEW, "Share article as image"),
                ChangelogItem(ChangeType.NEW, "Export category as PDF"),
                ChangelogItem(ChangeType.NEW, "In-app update checker")
            )
        )
    )
}
