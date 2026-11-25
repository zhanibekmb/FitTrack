package com.example.fittrack.ui.history

import androidx.lifecycle.ViewModel
import com.example.fittrack.data.model.CompletedSet
import com.example.fittrack.data.model.WorkoutSession
import com.example.fittrack.data.repository.FitTrackContainer
import com.example.fittrack.data.repository.WorkoutRepository

data class ExerciseResult(
    val session: WorkoutSession,
    val sets: List<CompletedSet>,
    val bestWeight: Double
)

class HistoryViewModel(repository: WorkoutRepository = FitTrackContainer.repository) : ViewModel() {
    val data = repository.data

    fun bestWeights(sessions: List<WorkoutSession>): Map<String, Double> {
        val weights = mutableMapOf<String, Double>()
        for (session in sessions) {
            for (set in session.completedSets) {
                val best = weights[set.exerciseId]
                if (best == null || set.weightKg > best) {
                    weights[set.exerciseId] = set.weightKg
                }
            }
        }
        return weights
    }

    fun exerciseResults(sessions: List<WorkoutSession>, exerciseId: String): List<ExerciseResult> {
        val results = mutableListOf<ExerciseResult>()
        for (session in sessions.sortedBy { it.finishedAt }) {
            val sets = session.completedSets.filter { it.exerciseId == exerciseId }
            if (sets.isNotEmpty()) {
                results.add(ExerciseResult(session, sets, sets.maxOf { it.weightKg }))
            }
        }
        return results
    }
}
