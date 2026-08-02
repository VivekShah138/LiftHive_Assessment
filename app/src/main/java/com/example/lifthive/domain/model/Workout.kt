package com.example.lifthive.domain.model

data class Workout(
    val id: Long = 0,
    val title: String,
    val date: Long,
    val notes: String,
    val exercises: List<Exercise> = emptyList()
)
