# Team Lead integration

The current graph deliberately uses placeholders. Do not import teammate classes until their modules compile.

## Proposed contracts (agree before replacing placeholders)

```kotlin
@Composable
fun PlansScreen(
    viewModel: PlansViewModel,
    onStartWorkout: (String) -> Unit,
    onBack: () -> Unit
)

@Composable
fun WorkoutScreen(
    planId: String,
    viewModel: WorkoutViewModel,
    onBack: () -> Unit
)

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBack: () -> Unit
)
```

- Construct each ViewModel with the same FitTrackContainer.repository, using a factory and the destination's ViewModelStoreOwner. Never instantiate a repository inside a screen or ViewModel.
- PlansScreen's callback passes a real plan ID. The graph currently owns start/resume and conflict feedback. PlansViewModel must not also call startWorkout on that callback.
- WorkoutViewModel observes activeSession; it must verify that its planId matches. Back navigation leaves the session active. Only successful finishWorkout clears it; an empty workout cannot finish.
- After process death the repository resets to demo data. A restored workout route does not guarantee an active session: show a start/recovery action rather than silently recording sets.
- Navigation decodes the planId argument. Do not decode it again. Use Routes.workout(id) to build paths; keep route identifiers unchanged.
- HistoryViewModel observes sessions and counts only finished sessions. Use completedSets for results, never target weights/reps from plans.
- Collect StateFlow with collectAsStateWithLifecycle in Compose. Do not call repository mutations during composition.
- The graph applies Scaffold content padding for system bars. Future screens should avoid applying the same system-bar padding twice.
- Schedule convention: Monday = 1, Sunday = 7. Dashboard checks local day every 30 seconds while observed.
- Repository is in memory, designed for sequential UI actions. Concurrent multi-thread mutations are not guaranteed atomic across its flows.

## Integration order

1. Merge the Team Lead PR after review and checks.
2. Each member updates their own branch from main, resolves their conflicts manually, and builds/tests their module.
3. Member 2 delivers PlansScreen/CreatePlanScreen and PlansViewModel with factory. Creation validates input and uses createPlan/addExerciseToPlan.
4. Member 3 delivers WorkoutScreen/WorkoutViewModel/RestTimer with factory, input validation, set completion and explicit workout completion.
5. Member 4 delivers HistoryScreen/HistoryViewModel/ProgressChart with factory and empty-history state.
6. Team Lead replaces one placeholder at a time and checks navigation and shared state after each integration.

## English terminology

Use string resources: Workout Plans, Create Plan, Start Workout, Resume Workout, Active Workout, Today's Workout, Workout History, Total Volume, Weight (kg), Repetitions, Sets, Rest Timer, Finish Workout, Back, No workout scheduled for today.

## Manual smoke checks

1. Launch on API 24 and a recent Android emulator. Check readable content above/below system bars, scrolling, and large fonts.
2. Home shows three demo plans and zero completed workouts/volume. Today's plans match the local weekday.
3. Open Plans, start Push Day, press Back, then try another plan: a conflict message appears. Starting Push Day again resumes it.
4. Return Home and use Resume Workout. Verify hardware Back and on-screen Back return to the previous screen without finishing the workout.
5. Open History and return. Rapid taps should not duplicate the current destination.
6. Rotate the device: active session survives Activity recreation. Kill/relaunch the process: expect demo reset, not persistence.
7. Keep Home open across midnight or change the local date: today's schedule refreshes within 30 seconds. Also check after background/foreground.
8. During future integration, test plan IDs containing spaces, slash, percent and question mark; missing plans show an error. Verify zero-weight sets, invalid input, duplicate sets, empty finish rejection, completed history and dashboard updates.

## Validation (PowerShell)

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
```
