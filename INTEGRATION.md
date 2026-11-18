# FitTrack integration contracts

Home, planning, workout tracking, history, and exercise progress are connected in
`FitTrackNavGraph`. All use `FitTrackContainer.repository`.

## Shared state and navigation

- Construct Home, Plans, and History ViewModels with their factories at destinations.
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
  during composition. The graph applies system-bar padding once.
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

The repository is in memory and intended for sequential UI actions. Its flows do
not provide atomic multi-thread transactions. Process death resets plans/history
to demo data and clears the session. A restored workout route therefore offers
explicit recovery. No database or background timer notifications are included.

The rest timer uses elapsed real time and a monotonic deadline. It remains active
when navigating to another screen while the application process survives.

## Validation

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-configuration-cache --console=plain
```

See `WORKOUT_MODULE.md` for the branch map and manual checks on API 24+ devices.
