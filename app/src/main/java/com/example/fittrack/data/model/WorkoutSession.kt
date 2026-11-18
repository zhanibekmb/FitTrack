package com.example.fittrack.data.model

data class WorkoutSession(
    val id: String,
    val planId: String,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val completedSets: List<CompletedSet> = emptyList()
) {
    val isFinished: Boolean
        get() = finishedAt != null
}
