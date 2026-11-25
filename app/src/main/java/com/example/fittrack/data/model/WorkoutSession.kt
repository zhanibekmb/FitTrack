package com.example.fittrack.data.model

data class WorkoutSession(
    val id: String,
    val planId: String,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val completedSets: List<CompletedSet> = emptyList(),
    val planName: String? = null
) {
    val isFinished: Boolean
        get() = finishedAt != null

    fun totalVolumeKg(): Double {
        return completedSets.sumOf { it.volumeKg }
    }
}
