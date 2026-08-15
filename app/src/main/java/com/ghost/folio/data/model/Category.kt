package com.ghost.folio.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val slug: String,
    val label: String,
    val articleCount: Int = 0
)
