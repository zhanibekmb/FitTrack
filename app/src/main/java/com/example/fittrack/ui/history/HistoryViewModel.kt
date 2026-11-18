package com.example.fittrack.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fittrack.data.repository.WorkoutRepository

class HistoryViewModel(repository: WorkoutRepository) : ViewModel() {
    val sessions = repository.sessions
    val plans = repository.plans
    val exercises = repository.exercises
}

class HistoryViewModelFactory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HistoryViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return HistoryViewModel(repository) as T
    }
}
