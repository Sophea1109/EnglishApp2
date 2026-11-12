package com.example.englishapp.data

import com.example.englishapp.model.Exercise
import com.example.englishapp.model.ExerciseSession
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class SupabaseExerciseRepository(private val db: SupabaseClient) : ExerciseRepository {
    private suspend fun sectionId(section: String): Int? =
        db.from("sections")
            .select { filter {eq("skill_type", section.lowercase()) } }
            .decodeSingleOrNull<SectionDto>()?.id

    override suspend fun getSessions(section: String): List<ExerciseSession> {
        val sid = sectionId(section) ?: return emptyList()
        return db.from("sessions")
            .select {
                filter { eq("section_id", sid) }
                order("display_order", Order.ASCENDING)
            }
            .decodeList<SessionDto>()
            .map {
                ExerciseSession(it.id, section, it.title ?: "Session ${it.displayOrder}", it.displayOrder)
            }
    }

    override suspend fun addSession(section: String, title: String, sessionOrder: Int) {
        val sid = sectionId(section) ?: error("Section '$section' doesn't exist in the database yet")
        db.from("sessions").insert(SessionInsert(sid, sessionOrder, title))
    }

    override suspend fun deleteSession(sessionId: Int) {
        db.from("sessions").delete { filter { eq("id", sessionId) } }
    }

    //Exercise
    override suspend fun getExercises(sessionId: Int): List<Exercise> =
        emptyList()

    override suspend fun addExercise(
        sessionId: Int,
        type: String,
        category: String,
        prompt: String,
        payload: String
    ) {
        throw NotImplementedError("Exercises not connected yet")
    }

    override suspend fun deleteExercise(exerciseId: Int) {
        throw NotImplementedError("Exercises not connected yet")
    }
}