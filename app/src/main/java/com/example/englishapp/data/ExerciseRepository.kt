package com.example.englishapp.data

import com.example.englishapp.model.Exercise
import com.example.englishapp.model.ExerciseSession

// list of things on what the app can do, each function shows the inputs it needs and what it returns
// security guard: check whether all the methods are being used or not. If one is not, an error will appear
interface ExerciseRepository {
    // Units change to session
    suspend fun getSessions(section: String): List<ExerciseSession>
    suspend fun addSession(section: String, title: String, sessionOrder: Int)
    suspend fun deleteSession(sessionId: Int)

    // Exercises
    suspend fun getExercises(sessionId: Int): List<Exercise>
    suspend fun addExercise(sessionId: Int, type: String, category: String, prompt: String, payload: String)
    suspend fun deleteExercise(exerciseId: Int)
}