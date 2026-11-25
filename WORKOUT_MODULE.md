# Sabyrzhan — Workout Session & Rest Timer

## Workout Planning & Session Persistence

The updated assignment is implemented on `codex/workout-room-persistence`, based
on the current GitHub main. The older branch table below describes the previous
workout-session assignment.

| Updated task | Implementation |
| --- | --- |
| 1. WorkoutPlanEntity | `data/local/WorkoutPlanEntity.kt` |
| 2. PlannedExerciseEntity | `data/local/PlannedExerciseEntity.kt`, ordered composite key and cascading plan FK |
| 3. WorkoutPlanDao | `data/local/WorkoutPlanDao.kt`, Flow reads and suspend writes |
| 4. Create plan | `WorkoutRepository.createPlan`, `PlansViewModel`, form save callback after commit |
| 5. Read saved plans | `WorkoutPlanRepository.read`, DAO Flows, shared `WorkoutData` |
| 6. Update plan | Name/day plus add/edit/remove exercise targets; transactional child replacement |
| 7. Delete plan | Confirmation, active-plan guard, cascade planned exercises, preserve history |
| 8. Connect ViewModels | Existing shared repository now injects Room through `FitTrackContainer` |
| 9. Save Finish Workout | Session/set entities and one history transaction; failures retain the active session |
| 10. Survive restart | Disk-reopen instrumented tests and `scripts/check-workout-restart.ps1` |

Plans and completed results now persist in Room. Unfinished input and the rest
timer remain in memory. Demo plans seed only once. ViewModels report save errors
and prevent duplicate submissions; navigation happens after a successful commit.

For defense, follow a save through `PlansScreen` → `PlansViewModel` →
`WorkoutRepository` → `WorkoutPlanRepository` → `WorkoutPlanDao` → SQLite. Room
generates the DAO implementation through KSP. Suspend writes avoid blocking the
UI; Flow refreshes the shared state and Compose observes it with lifecycle-aware
collection. Explain why `@Transaction` groups a plan with its exercises, why a
plan ID plus position allows repeated exercises, and why history has no cascading
foreign key to the plan. DataStore remains the team's store for small settings;
Room stores the related workout records.

The sections below are the historical implementation notes for the earlier phase.

The task branches form a stack: each branch includes the earlier tasks. The
complete implementation is on `feature/workout-viewmodel-integration`.

| Assignment task | Branch |
| --- | --- |
| 1. WorkoutScreen | `feature/workout-screen` |
| 2. ExerciseCard and SetRow | `feature/workout-components` |
| 3. Exercises from the selected plan | `feature/workout-plan-exercises` |
| 4. Actual weight input | `feature/workout-weight-input` |
| 5. Actual repetitions input | `feature/workout-repetitions` |
| 6. Set completion and corrections | `feature/workout-set-completion` |
| 7. Additional sets | `feature/workout-extra-sets` |
| 8. Previous results | `feature/workout-previous-results` |
| 9. Rest timer | `feature/workout-rest-timer` |
| 10. ViewModel, finished results and integration | `feature/workout-viewmodel-integration` |

`feature/history-build-compatibility` is the prerequisite that replaces the history
module's incompatible mock models and missing helpers with shared repository data.

## Design

- One `WorkoutViewModel` owns the current workout's input and a small coroutine timer.
- Completed results are stored immediately in the shared `WorkoutRepository`.
- Unchecking removes a completed result so its fields can be corrected.
- Planned targets are read only during a workout; drafts and extra rows belong to the editor.
- Repeated occurrences of an exercise share one card; all targets are retained and set numbers are unique.
- Previous matches exercise ID and set number from the latest finished session with that exercise.
- The timer uses a monotonic deadline rather than counting ticks. It stays in the same ViewModel when navigating back.
- No Room, dependency injection framework, services, or chart libraries are introduced.

## Manual smoke checks

1. Start Push Day. Check the exercise names and original targets.
2. Enter `17,5` kg and 8 reps. Complete the set; the rest timer should start.
3. Tap the completed checkbox again, correct the reps, and complete it. There should be one recorded result.
4. Try an empty weight, negative weight, and zero reps. Each invalid field should show an error when completing the set.
5. Add a fourth set. Complete it and verify that the program still has its original three planned sets.
6. Use 60/90/120-second presets and a custom duration. Check pause, resume, reset, skip, and completion text.
7. Press Back, return through Resume Workout, and rotate. Check drafts, added rows, completed sets, and the running timer.
8. Try starting a different program while this one is active. The current workout must remain active.
9. Finish with some rows unchecked. Confirm that only checked rows appear in history and dashboard volume.
10. Start another Push Day and check Previous, including a fourth set added last time.
11. Check zero extra weight for Pull-ups, an empty plan, and a repeated exercise in one plan.
12. Discard a workout and verify that it adds no history. Kill/relaunch the process: expect demo reset and explicit recovery.

## Automated checks

The unit suite covers input validation, repeated exercise targets, completed extra
set restoration, plan/result separation, corrections, duplicate taps, finish and
discard behavior, missing sessions, previous results, draft retention, and delayed
timer ticks. Run the command in README before integration.

## Latest main integration

The final branch includes `aa8ed09` (Material Design implemented). Shared app bars,
English resources, planning screens, and history/progress styling are preserved.
Workout tracking continues to use its ViewModel, Previous, corrections, extra sets,
and configurable rest timer. The connected workout-flow test follows this UI.
