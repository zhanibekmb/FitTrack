package com.example.fittrack.navigation

object Routes {
    const val HOME = "home"
    const val PLANS = "plans"
    const val HISTORY = "history"
    const val WORKOUT = "workout/{planId}"

    fun workout(planId: String): String {
        return "workout/$planId"
    }
}