package com.example.fittrack.ui.home

import androidx.lifecycle.ViewModel
import com.example.fittrack.data.repository.FitTrackContainer
import com.example.fittrack.data.repository.WorkoutData
import java.util.Calendar

class HomeViewModel : ViewModel() {
    val data = FitTrackContainer.repository.data

    fun currentTrainingDay(): Int {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        if (day == Calendar.SUNDAY) return 7
        return day - 1
    }

    fun dashboard(data: WorkoutData, day: Int): HomeUiState {
        return dashboardState(data.plans, data.sessions, data.activeSession, day)
    }
}
