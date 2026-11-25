# FitTrack integration contracts

Home, planning, workout tracking, history, and exercise progress are connected in
`FitTrackNavGraph`. All use `FitTrackContainer.repository`.

## Shared state and navigation

- Create Home, Plans, and History ViewModels at their destinations using `viewModel()`.
- `MainActivity` owns the `SettingsViewModel` and passes its state and theme callback to navigation. DataStore saves the theme across process restarts.
- Construct the single `WorkoutViewModel` above `NavHost`, using the graph's
  inherited Activity owner. This deliberately keeps drafts, added rows, and the
  timer alive when Back pops the workout destination. Rotation keeps this owner.
- Do not instantiate a repository inside a screen or ViewModel.
- Navigation owns normal start/resume and conflict feedback. The workout screen
  only starts a session through an explicit recovery action if no session exists.
- The workout screen verifies its route's `planId` against the active session.
  Finishing/discarding also verifies the session ID before changing repository data.
- Back does not finish a session. Successful finish saves checked sets to history;
  confirmed discard clears the session without adding history. Empty plans cannot start.
- Empty workouts cannot finish. Finishing a partially completed plan is allowed
  after confirmation and stores only completed sets.
- Use `Routes.workout(id)`, `Routes.planDetail(id)`, and `Routes.exerciseProgress(id)`
  to build routes. Navigation decodes arguments; do not decode them a second time.
- Collect StateFlow using `collectAsStateWithLifecycle`. Do not mutate the repository
  during composition. The graph applies the common Material app bar and system-bar
  padding once; child screens do not add duplicate headers or Back buttons.
- Monday = 1, Sunday = 7. Dashboard refreshes the local day while observed.

## Workout results

- Shared models live in `data.model`. History must not define a second session model.
- `CompletedSet` represents a confirmed result, keyed by exercise ID and set number.
- `completeSet` rejects invalid numbers, exercises outside the current plan, and duplicates.
- `removeCompletedSet` supports unchecking and correction. Completed fields are otherwise locked.
- Repeated plan entries for one exercise are combined into a card with consecutive
  set numbers, preserving the targets of each entry.
- Extra rows and unconfirmed input are ViewModel state. They never change plan targets.
- Previous comes from the latest finished session with the exercise, across all plans;
  match by set number and display a dash when no previous result exists.
- History and dashboard statistics use completed results, never planned weights/reps.

## Lifetime and limits

`MainActivity` initializes `FitTrackContainer` before displaying navigation. The
container caches one application-context Room database and one shared workout
repository. Startup waits for the first database read and offers Retry on failure.
`WorkoutPlanRepository` implements `WorkoutStorage`: suspend transactions write
plans/results, and DAO Flows invalidate the shared state. Persistent writes are
serialized; success is reported after the transaction commits. ViewModels launch
coroutines and keep their input when saving fails. Plan mutations are blocked
while that plan has an active session. Session/set insertion is one transaction;
deleting a plan cascades only its planned exercises and keeps finished history.

Plans/history survive process death. Only the active session, drafts, extra rows,
and rest timer are in memory. A restored workout route offers explicit recovery.
The no-storage `WorkoutRepository()` constructor supplies demo state for JVM tests;
production always injects Room. See [TEAM_INTEGRATION.md](TEAM_INTEGRATION.md).

The rest timer uses elapsed real time and a monotonic deadline. It remains active
when navigating to another screen while the application process survives.

## Validation

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-configuration-cache --console=plain
```

See `WORKOUT_MODULE.md` for the branch map and manual checks on API 24+ devices.
