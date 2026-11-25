# FitTrack integration

All ViewModels share the in-memory `FitTrackContainer.repository`. Destinations collect its `WorkoutData` through their ViewModels.

## Contracts

- Create ViewModels with `viewModel()`. Workout actions share the app-level `WorkoutViewModel`.
- `WorkoutViewModel` handles workout start and resume. Navigation displays conflict messages. Back leaves the current session active. Finish Workout publishes history and clears the active session together.
- Empty plans cannot start through navigation. Empty sessions cannot finish. Validate finite nonnegative weights and positive whole-number repetitions and sets.
- History displays finished workouts. Charts show one best-weight point per workout in chronological order. Plan deletion keeps history and is blocked for an active plan.
- Repository data is held in memory. Activity recreation preserves it; process death resets plans and workout history to demo data.
- Route arguments are decoded by Navigation. Use route builders to encode identifiers once.
- Collect StateFlow with `collectAsStateWithLifecycle`. Do not mutate the repository during composition.
- The root Scaffold owns the app bar and system-bar insets. Screens own scrollable content padding, and forms handle keyboard insets.
- Schedule convention: Monday = 1, Sunday = 7. Home refreshes the local day every 30 seconds while observed.
- Repository mutations are intended for sequential UI actions.

## Language and styling

App resources and bundled component resources are English only. MainActivity supplies an English resource configuration independently of the device language. Dates and chart descriptions explicitly use English formatting. User-entered plan names are preserved.

Use Material 3 components, theme color roles, the Material typography scale, shared shapes, 20 dp screen padding, 16 dp card padding, and 8/12/16 dp spacing. Android 12+ uses system dynamic colors; older versions use the green palette. Support both light and dark themes.

## Validation

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:connectedDebugAndroidTest --console=plain
```

The connected test requires a running emulator or device. It verifies recording a set, finishing, opening history and progress, and English app resources. Unit tests cover repository validation, dashboard statistics, and shared history data.

Manual checks: launch with a non-English device language; check light/dark themes, landscape, large fonts, search, plan creation, exercise input validation, rest timer, active-workout conflicts, back/resume, and process-death recovery.
