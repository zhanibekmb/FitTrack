package com.example.fittrack.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fittrack.data.repository.WorkoutRepository

class WorkoutViewModelFactory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(WorkoutViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return WorkoutViewModel(repository) as T
    }
}
