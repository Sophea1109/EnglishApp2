package com.example.englishapp.data

import com.example.englishapp.model.Exercise
import com.example.englishapp.model.ExerciseSession

// useful for tesing without internet
object FakeExerciseRepository : ExerciseRepository {

    private val sessions = mutableListOf(
        ExerciseSession(1, "Vocabulary", "Unit 1", 1),
        ExerciseSession(2, "Grammar", "Unit 3", 2),
        ExerciseSession(3, "Reading", "Unit 5", 3)
        // Speaking has none on purpose: lets you test the empty message
    )
    private val exercises = mutableListOf<Exercise>()

    // Supabase will create ids itself, so the fake does the same
    private var nextSessionId = 4
    private var nextExerciseId = 1

    override suspend fun getSessions(section: String): List<ExerciseSession> =
        sessions.filter { it.section == section }.sortedBy { it.sessionOrder }

    override suspend fun addSession(section: String, title: String, sessionOrder: Int) {
        sessions.add(ExerciseSession(nextSessionId++, section, title, sessionOrder))
    }

    override suspend fun deleteSession(sessionId: Int) {
        sessions.removeAll { it.id == sessionId }
        exercises.removeAll { it.sessionId == sessionId }   // in Supabase: "on delete cascade"
    }

    override suspend fun getExercises(sessionId: Int): List<Exercise> =
        exercises.filter { it.sessionId == sessionId }

    override suspend fun addExercise(
        sessionId: Int, type: String, category: String, prompt: String, payload: String
    ) {
        exercises.add(Exercise(nextExerciseId++, sessionId, type, category, prompt, payload))
    }

    override suspend fun deleteExercise(exerciseId: Int) {
        exercises.removeAll { it.id == exerciseId }
    }
}