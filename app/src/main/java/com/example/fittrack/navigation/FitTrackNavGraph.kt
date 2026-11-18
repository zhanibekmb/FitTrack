
package com.example.fittrack.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.example.fittrack.data.repository.FitTrackContainer
import com.example.fittrack.ui.home.HomeScreen
import com.example.fittrack.ui.home.HomeViewModel
import com.example.fittrack.ui.home.HomeViewModelFactory

@Composable
fun FitTrackNavGraph() {

    val navController = rememberNavController()

    // Shared Repository for all screens
    val repository = FitTrackContainer.repository

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {

        // HOME
        composable(Routes.HOME) {

            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(repository)
            )

            val uiState by homeViewModel.uiState
                .collectAsStateWithLifecycle()

            HomeScreen(
                uiState = uiState,

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

            val plans by repository.plans
                .collectAsStateWithLifecycle()

            PlansPlaceholderScreen(
                plans = plans,

                onStartWorkout = { planId ->

                    val active = repository.activeSession.value

                    if (active == null) {

                        val session = repository.startWorkout(planId)

                        if (session != null) {
                            navController.navigate(
                                Routes.workout(planId)
                            )
                        }

                    } else if (active.planId == planId) {

                        // Resume existing workout
                        navController.navigate(
                            Routes.workout(planId)
                        )
                    }
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
