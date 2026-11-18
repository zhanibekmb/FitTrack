package com.example.fittrack.navigation

import android.net.Uri

object Routes {
    const val HOME = "home"
    const val PLANS = "plans"
    const val HISTORY = "history"
    const val PLAN_ID = "planId"
    const val WORKOUT = "workout/{planId}"

    fun workout(planId: String): String {
        require(planId.isNotBlank()) { "Plan ID cannot be blank" }
        return "workout/${Uri.encode(planId)}"
    }
}
