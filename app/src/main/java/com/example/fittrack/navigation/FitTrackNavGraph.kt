package com.example.fittrack.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
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
import com.example.fittrack.ui.history.*
import com.example.fittrack.ui.workout.*
import com.example.fittrack.ui.components.FitTrackAppBar
import kotlinx.coroutines.launch

@Composable
fun FitTrackNavGraph() {
    val navController = rememberNavController()
    val repository = FitTrackContainer.repository
    val workoutViewModel: WorkoutViewModel = viewModel(factory = WorkoutViewModelFactory(repository))
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val conflictMessage = stringResource(R.string.workout_conflict)
    val invalidMessage = stringResource(R.string.invalid_plan)
    val emptyMessage = stringResource(R.string.empty_plan_hint)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: Routes.HOME
    val title = stringResource(when (route) {
        Routes.PLANS -> R.string.workout_plans
        Routes.CREATE_PLAN -> R.string.create_plan
        Routes.PLAN_DETAIL -> R.string.workout
        Routes.HISTORY -> R.string.workout_history
        Routes.EXERCISE_PROGRESS -> R.string.exercise_progress
        Routes.WORKOUT -> R.string.active_workout
        else -> R.string.app_name
    })

    fun openWorkout(planId: String) {
        val active = repository.activeSession.value
        val message = when {
            repository.plans.value.none { it.id == planId } -> invalidMessage
            active != null && active.planId != planId -> conflictMessage
            active == null && repository.plans.value.find { it.id == planId }?.exercises.isNullOrEmpty() -> emptyMessage
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
    Scaffold(
        topBar = { FitTrackAppBar(title, if (route == Routes.HOME) null else ({ navController.popBackStack(); Unit })) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding)
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
                    onCreatePlan = { navController.navigate(Routes.CREATE_PLAN) { launchSingleTop = true } }
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
                    }
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
                    onStartWorkout = { openWorkout(planId) }
                )
            }
            composable(Routes.HISTORY) {
                val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(repository))
                HistoryScreen(historyViewModel, openExercise = {
                    navController.navigate(Routes.exerciseProgress(it)) { launchSingleTop = true }
                })
            }
            composable(Routes.EXERCISE_PROGRESS,
                arguments = listOf(navArgument(Routes.EXERCISE_ID) { type = NavType.StringType })) { entry ->
                val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(repository))
                ExerciseProgressScreen(entry.arguments?.getString(Routes.EXERCISE_ID).orEmpty(), historyViewModel)
            }
            composable(
                route = Routes.WORKOUT,
                arguments = listOf(navArgument(Routes.PLAN_ID) { type = NavType.StringType })
            ) { entry ->
                val planId = entry.arguments?.getString(Routes.PLAN_ID)
                val plans by repository.plans.collectAsStateWithLifecycle()
                if (planId.isNullOrBlank() || plans.none { it.id == planId }) {
                    Text(stringResource(R.string.invalid_plan), modifier = Modifier.padding(20.dp))
                } else {
                    WorkoutScreen(
                        planId = planId,
                        viewModel = workoutViewModel,
                        onFinished = { navController.popBackStack(Routes.HOME, inclusive = false) }
                    )
                }
            }
        }
    }
}
