package com.example.fittrack.ui.history

import androidx.lifecycle.ViewModel
import com.example.fittrack.data.repository.WorkoutRepository

class HistoryViewModel(repository: WorkoutRepository) : ViewModel() {
    val sessions = repository.sessions
}
