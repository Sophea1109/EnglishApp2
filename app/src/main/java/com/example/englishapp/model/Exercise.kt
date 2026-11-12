package com.example.englishapp.model

data class Exercise (
    val id: Int,
    val sessionId: Int,
    val type: String,
    val category: String,
    val prompt: String,
    val payload: String
)