package com.example.englishapp.ui

import androidx.lifecycle.ViewModel
import com.example.englishapp.data.ExerciseRepository
import com.example.englishapp.data.Supabase
import com.example.englishapp.data.SupabaseExerciseRepository

// picking either the fake or Supabase
// hold one repo where all fragments calls it
class AdminViewModel : ViewModel() {
    val repo: ExerciseRepository = SupabaseExerciseRepository(Supabase.client)

    // Remembers which session chip is selected
    var selectedSection: String = "Vocabulary"
}