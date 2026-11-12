package com.example.englishapp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Classes shaped like the database rows, so Supabase can convert JSON to kotlin objects and back
// map database name to kotlin name
@Serializable
data class SectionDto (
    val id: Int,
    @SerialName("skill_type") val skillType: String
)

@Serializable
data class SessionDto (
    val id: Int,
    @SerialName("section_id") val sectionId: Int,
    @SerialName("display_order") val displayOrder: Int,
    val title: String? = null
)

@Serializable
data class SessionInsert(
    @SerialName("section_id") val sectionId: Int,
    @SerialName("display_order") val displayOrder: Int,
    val title: String
)