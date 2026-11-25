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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.fittrack.ui.home.*
import com.example.fittrack.ui.plans.*
import com.example.fittrack.ui.history.*
import com.example.fittrack.ui.workout.WorkoutScreen
import com.example.fittrack.ui.workout.WorkoutStartResult
import com.example.fittrack.ui.workout.WorkoutViewModel
import kotlinx.coroutines.delay
import com.example.fittrack.ui.components.FitTrackAppBar
import kotlinx.coroutines.launch

@Composable
fun FitTrackNavGraph() {
    val navController = rememberNavController()
    val workoutViewModel: WorkoutViewModel = viewModel()
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
        Routes.EDIT_PLAN -> R.string.edit_plan
        Routes.PLAN_DETAIL -> R.string.workout
        Routes.HISTORY -> R.string.workout_history
        Routes.EXERCISE_PROGRESS -> R.string.exercise_progress
        Routes.WORKOUT -> R.string.active_workout
        else -> R.string.app_name
    })

    fun openWorkout(planId: String) {
        val message = when (workoutViewModel.startOrResume(planId)) {
            WorkoutStartResult.INVALID_PLAN -> invalidMessage
            WorkoutStartResult.OTHER_WORKOUT_ACTIVE -> conflictMessage
            WorkoutStartResult.EMPTY_PLAN -> emptyMessage
            WorkoutStartResult.READY -> null
        }
        if (message != null) {
            scope.launch { snackbar.showSnackbar(message) }
        } else {
            navController.navigate(Routes.workout(planId)) { launchSingleTop = true }
        }
    }

    val goBack: () -> Unit = { navController.popBackStack() }
    Scaffold(
        topBar = { FitTrackAppBar(title, if (route == Routes.HOME) null else goBack) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding)
        ) {
            composable(Routes.HOME) {
                val homeViewModel: HomeViewModel = viewModel()
                val data by homeViewModel.data.collectAsStateWithLifecycle()
                val lifecycle = LocalLifecycleOwner.current.lifecycle
                var today by remember { mutableIntStateOf(homeViewModel.currentTrainingDay()) }
                LaunchedEffect(lifecycle) {
                    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        while (true) {
                            today = homeViewModel.currentTrainingDay()
                            delay(30_000)
                        }
                    }
                }
                HomeScreen(
                    uiState = homeViewModel.dashboard(data, today),
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
                val plansViewModel: PlansViewModel = viewModel()
                PlansScreen(
                    viewModel = plansViewModel,
                    onOpenPlan = { navController.navigate(Routes.planDetail(it)) { launchSingleTop = true } },
                    onCreatePlan = { navController.navigate(Routes.CREATE_PLAN) { launchSingleTop = true } }
                )
            }
            composable(Routes.CREATE_PLAN) {
                val plansViewModel: PlansViewModel = viewModel()
                PlanFormScreen(
                    viewModel = plansViewModel,
                    onSaved = { id ->
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
                val plansViewModel: PlansViewModel = viewModel()
                val planId = entry.arguments?.getString(Routes.PLAN_ID).orEmpty()
                PlanDetailScreen(
                    viewModel = plansViewModel,
                    planId = planId,
                    onStartWorkout = { openWorkout(planId) },
                    onEditPlan = { navController.navigate(Routes.editPlan(planId)) },
                    onDeleted = goBack
                )
            }
            composable(
                route = Routes.EDIT_PLAN,
                arguments = listOf(navArgument(Routes.PLAN_ID) { type = NavType.StringType })
            ) { entry ->
                val plansViewModel: PlansViewModel = viewModel()
                val planId = entry.arguments?.getString(Routes.PLAN_ID).orEmpty()
                val data by plansViewModel.data.collectAsStateWithLifecycle()
                val plan = data.plans.find { it.id == planId }
                if (plan == null) {
                    Text(stringResource(R.string.invalid_plan), modifier = Modifier.padding(20.dp))
                } else {
                    PlanFormScreen(plansViewModel, onSaved = { goBack() }, plan = plan)
                }
            }
            composable(Routes.HISTORY) {
                val historyViewModel: HistoryViewModel = viewModel()
                HistoryScreen(historyViewModel, openExercise = {
                    navController.navigate(Routes.exerciseProgress(it)) { launchSingleTop = true }
                })
            }
            composable(Routes.EXERCISE_PROGRESS,
                arguments = listOf(navArgument(Routes.EXERCISE_ID) { type = NavType.StringType })) { entry ->
                val historyViewModel: HistoryViewModel = viewModel()
                ExerciseProgressScreen(entry.arguments?.getString(Routes.EXERCISE_ID).orEmpty(), historyViewModel)
            }
            composable(
                route = Routes.WORKOUT,
                arguments = listOf(navArgument(Routes.PLAN_ID) { type = NavType.StringType })
            ) { entry ->
                val planId = entry.arguments?.getString(Routes.PLAN_ID).orEmpty()
                WorkoutScreen(
                    planId = planId,
                    viewModel = workoutViewModel,
                    onStart = { openWorkout(planId) },
                    onFinished = goBack
                )
            }
        }
    }
}
