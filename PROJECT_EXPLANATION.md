# FitTrack project explanation

FitTrack is a local gym workout journal. You choose a training plan, record the weight and repetitions for each set, and finish the workout to see it in History and Exercise Progress.

## How to use it

1. Open **Workout Plans**. Search for a plan or create one by entering a name and choosing a training day.
2. Open a plan. Add an exercise from the catalog and enter its target sets, repetitions, and weight. Use **Edit plan** to change the name or day. Use **Delete plan** to remove the plan after confirming.
3. Start a workout. The targets fill in the initial recording inputs. Record sets with the actual weight and repetitions. Recording a set starts a one-minute rest timer. You can also start or skip a rest manually.
4. If you go back, the active workout is still available under **Resume workout** on Home. Finish the workout after recording at least one set to add it to History.
5. In History, open an exercise to see its best weight and its results in previous workouts.

The app starts with three sample plans and nine catalog exercises. History starts empty. Plans and workout history live in memory for the current Android process. Rotating the device keeps the shared data. If Android ends the process, the next launch starts with the demo plans and empty history. The OS may keep an app process alive after you leave it.

## Project layout

| File or group | Responsibility |
| --- | --- |
| `MainActivity.kt` | Starts Compose, selects the app's English resource configuration, and applies edge-to-edge and the Material theme. |
| `navigation/Routes.kt` | Defines the route strings and builds links containing plan or exercise IDs. |
| `navigation/FitTrackNavGraph.kt` | Connects routes to screens, reads route arguments, handles Back, and displays workout-start feedback in the shared app bar and Snackbar layout. |
| `data/DemoData.kt` | Defines the starting plans and exercise catalog. |
| `data/model/Exercise.kt` | Identifies a catalog exercise by ID, name, and muscle group. |
| `data/model/PlannedExercise.kt` | Links an exercise to its targets in a plan. |
| `data/model/WorkoutPlan.kt` | Holds a plan's ID, name, scheduled day, and planned exercises. |
| `data/model/CompletedSet.kt` | Holds one recorded set and calculates its volume as weight times repetitions. |
| `data/model/WorkoutSession.kt` | Holds the sets and timestamps for one workout and adds up its total volume. |
| `data/repository/FitTrackContainer.kt` | Creates the single in-memory data holder shared by the app's ViewModels. |
| `data/repository/WorkoutRepository.kt` | Defines `WorkoutData` and changes plans, the active workout, and history. Its public StateFlow lets screens observe changes through their ViewModels. |
| `ui/home/HomeViewModel.kt`, `DashboardState.kt`, and `HomeUiState.kt` | Convert shared data into the totals, today's plan list, and active workout details displayed on Home. |
| `ui/home/HomeScreen.kt` | Shows dashboard cards and entry points to plans, history, and the active workout. |
| `ui/plans/PlansViewModel.kt` | Handles create, edit, delete, and add-exercise actions. |
| `ui/plans/PlansScreen.kt` and `PlanDetailScreen.kt` | Show the searchable plan list, the shared create/edit form, the exercise list, and the add/delete dialogs. |
| `ui/workout/WorkoutViewModel.kt` | Starts or resumes a workout, records sets, and finishes the session. |
| `ui/workout/WorkoutScreen.kt` | Shows exercise targets, input fields, recorded sets, the rest countdown, and the Finish button. |
| `ui/history/HistoryViewModel.kt` | Finds each exercise's best weight and prepares chronological progress results, one result per workout. |
| `ui/history/HistoryScreen.kt`, `ExerciseProgressScreen.kt`, and `ProgressChart.kt` | Show completed workouts, lists of recorded sets, and the best weight per workout on the chart. |
| `ui/components/` and `ui/theme/` | Provide the shared app bar, stat cards, empty states, palette, and shapes. Material 3 supplies the typography. |
| `res/`, the manifest, and Gradle files | Provide local resources, application configuration, and build dependencies. |
| `src/test/` and `src/androidTest/` | Verify calculations and data rules on the JVM, and run screen flows on Android. |

## How a tap updates the app

1. The screen reads its input fields and calls a ViewModel function.
2. The ViewModel passes the action to `WorkoutRepository`.
3. The repository validates it and creates an updated copy of `WorkoutData`.
4. Publishing the copy through StateFlow tells Compose that the data changed. Screens collect the flow with `collectAsStateWithLifecycle`. Home turns that data into a smaller `HomeUiState`.
5. Compose displays the new values. Temporary inputs like a search query or dialog fields belong to the screen and use `rememberSaveable`.

For example, finishing a workout appends it to history and clears the active session in one update. Deleting a plan is blocked while it has an active workout. Finished workouts keep their recorded sets and a fallback plan name, so they still make sense after the plan is deleted.

