package com.example.fittrack.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fittrack.ui.home.HomeScreen

@Composable
fun FitTrackNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        // HOME
        composable(Routes.HOME) {
            HomeScreen(
                onOpenPlans = {
                    navController.navigate(Routes.PLANS)
                },
                onOpenHistory = {
                    navController.navigate(Routes.HISTORY)
                }
            )
        }

        // PLANS
        composable(Routes.PLANS) {
            PlansPlaceholderScreen(
                onStartWorkout = { planId ->
                    navController.navigate(
                        Routes.workout(planId)
                    )
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // HISTORY
        composable(Routes.HISTORY) {
            HistoryPlaceholderScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // WORKOUT
        composable(
            route = Routes.WORKOUT,
            arguments = listOf(
                navArgument("planId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val planId = backStackEntry.arguments
                ?.getString("planId")
                .orEmpty()

            WorkoutPlaceholderScreen(
                planId = planId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}