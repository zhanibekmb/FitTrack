package com.example.fittrack.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.fittrack.R
import com.example.fittrack.data.repository.FitTrackContainer
import com.example.fittrack.ui.home.*
import com.example.fittrack.ui.plans.*
import com.example.fittrack.ui.workout.*
import com.example.fittrack.ui.history.*
import kotlinx.coroutines.launch

@Composable
fun FitTrackNavGraph() {
    val navController = rememberNavController()
    val repository = FitTrackContainer.repository
    // One active workout: keep drafts and its timer when its destination is popped.
    val workoutViewModel: WorkoutViewModel = viewModel(factory = WorkoutViewModelFactory(repository))
    val emptyPlanMessage = stringResource(R.string.workout_empty_plan)
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val conflictMessage = stringResource(R.string.workout_conflict)
    val invalidMessage = stringResource(R.string.invalid_plan)

    fun openWorkout(planId: String) {
        val active = repository.activeSession.value
        val message = when {
            repository.plans.value.none { it.id == planId } -> invalidMessage
            repository.plans.value.first { it.id == planId }.exercises.isEmpty() -> emptyPlanMessage
            active != null && active.planId != planId -> conflictMessage
            active == null && repository.startWorkout(planId) == null -> invalidMessage
            else -> null
        }
        if (message != null) {
            scope.launch { snackbar.showSnackbar(message) }
        } else {
            navController.navigate(Routes.workout(planId)) { launchSingleTop = true }
        }
    }

    // Apply system-bar insets once for all current screens.
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(repository)
                )
                val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
                HomeScreen(
                    uiState = uiState,
                    onOpenPlans = {
                        navController.navigate(Routes.PLANS) { launchSingleTop = true }
                    },
                    onOpenHistory = {
                        navController.navigate(Routes.HISTORY) { launchSingleTop = true }
                    },
                    onResumeWorkout = { openWorkout(it) }
                )
            }
            composable(Routes.PLANS) {
                val plansViewModel: PlansViewModel = viewModel(factory = PlansViewModelFactory(repository))
                PlansScreen(
                    viewModel = plansViewModel,
                    onOpenPlan = { navController.navigate(Routes.planDetail(it)) { launchSingleTop = true } },
                    onCreatePlan = { navController.navigate(Routes.CREATE_PLAN) { launchSingleTop = true } },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.CREATE_PLAN) {
                val plansViewModel: PlansViewModel = viewModel(factory = PlansViewModelFactory(repository))
                CreatePlanScreen(
                    viewModel = plansViewModel,
                    onCreated = { id ->
                        navController.navigate(Routes.planDetail(id)) {
                            popUpTo(Routes.CREATE_PLAN) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.PLAN_DETAIL,
                arguments = listOf(navArgument(Routes.PLAN_ID) { type = NavType.StringType })
            ) { entry ->
                val plansViewModel: PlansViewModel = viewModel(factory = PlansViewModelFactory(repository))
                val planId = entry.arguments?.getString(Routes.PLAN_ID).orEmpty()
                PlanDetailScreen(
                    viewModel = plansViewModel,
                    planId = planId,
                    onStartWorkout = { openWorkout(planId) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.HISTORY) {
                val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(repository))
                HistoryScreen(historyViewModel,
                    onOpenExercise = { navController.navigate(Routes.exerciseProgress(it)) { launchSingleTop = true } },
                    onBack = { navController.popBackStack() })
            }
            composable(
                route = Routes.EXERCISE_PROGRESS,
                arguments = listOf(navArgument(Routes.EXERCISE_ID) { type = NavType.StringType })
            ) { entry ->
                val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(repository))
                ExerciseProgressScreen(entry.arguments?.getString(Routes.EXERCISE_ID).orEmpty(), historyViewModel,
                    onBack = { navController.popBackStack() })
            }
            composable(
                route = Routes.WORKOUT,
                arguments = listOf(navArgument(Routes.PLAN_ID) { type = NavType.StringType })
            ) { entry ->
                val planId = entry.arguments?.getString(Routes.PLAN_ID)
                val plans by repository.plans.collectAsStateWithLifecycle()
                if (planId.isNullOrBlank() || plans.none { it.id == planId }) {
                    InvalidPlanPlaceholderScreen(onBack = { navController.popBackStack() })
                } else {
                    WorkoutScreen(
                        planId = planId,
                        viewModel = workoutViewModel,
                        onBack = { navController.popBackStack() },
                        onFinished = { navController.popBackStack(Routes.HOME, inclusive = false) }
                    )
                }
            }
        }
    }
}
