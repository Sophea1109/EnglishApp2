package com.example.englishapp.model

enum class ExerciseType(val key: String, val label: String) {
//    ---------------------------------
    MULTIPLE_CHOICE("multiple_choice", "Multiple choice"),
    MATCH_MEANING("match_meaning", "Match meaning"),
    COMPLETE_BLANK("complete_blank", "Complete the blank"),
    SCRAMBLE("scramble", "Scramble"),
    REPEAT_PHRASE("repeat_phrase", "Repeat the phrase");
    //    ---------------------------------

    companion object {
        fun fromKey(key: String): ExerciseType? =
            ExerciseType.entries.firstOrNull { it.key == key }

        // Which types each section offers (same as your prototype)
        fun forSection(section: String): List<ExerciseType> = when (section) {
            "Vocabulary" -> listOf(MULTIPLE_CHOICE, MATCH_MEANING, COMPLETE_BLANK)
            "Grammar" -> listOf(SCRAMBLE, COMPLETE_BLANK)
            "Reading" -> listOf(SCRAMBLE, MULTIPLE_CHOICE)
            "Speaking" -> listOf(REPEAT_PHRASE)
            else -> ExerciseType.entries.toList()
        }
    }
}