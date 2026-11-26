package com.example.fittrack.navigation

import android.net.Uri

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val PLANS = "plans"
    const val HISTORY = "history"
    const val EXERCISE_PROGRESS = "history/exercise/{exerciseId}"
    const val EXERCISE_ID = "exerciseId"

    fun exerciseProgress(exerciseId: String) = "history/exercise/${Uri.encode(exerciseId)}"
    const val CREATE_PLAN = "plans/create"
    const val EDIT_PLAN = "plans/edit/{planId}"
    const val PLAN_DETAIL = "plans/detail/{planId}"
    const val EXERCISE_CATALOG = "plans/exercise-catalog/{planId}"

    fun exerciseCatalog(planId: String) = "plans/exercise-catalog/${Uri.encode(planId)}"

    fun editPlan(planId: String): String {
        return "plans/edit/${Uri.encode(planId)}"
    }

    fun planDetail(planId: String): String {
        require(planId.isNotBlank()) { "Plan ID cannot be blank" }
        return "plans/detail/${Uri.encode(planId)}"
    }
    const val PLAN_ID = "planId"
    const val WORKOUT = "workout/{planId}"

    fun workout(planId: String): String {
        require(planId.isNotBlank()) { "Plan ID cannot be blank" }
        return "workout/${Uri.encode(planId)}"
    }
}
