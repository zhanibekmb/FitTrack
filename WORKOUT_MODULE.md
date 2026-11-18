# Sabyrzhan — Workout Session & Rest Timer

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
