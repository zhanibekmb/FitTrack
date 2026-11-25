# FitTrack

Your workouts. Your progress.

FitTrack is an Android gym workout journal.

## Features
- Workout planning
- Workout tracking
- Rest timer
- Workout history
- Progress statistics
- Room storage for plans, planned exercises, and finished workout sets
- Persistent light, dark, or system appearance

## Tech Stack
- Kotlin
- Jetpack Compose
- MVVM
- StateFlow
- Navigation Compose
- Room, KSP, and Coroutines
- Preferences DataStore

## Team
- Mukatay Zhanibek: Team Lead, Architecture & Navigation
- Zhangazin Akniyet: Workout Planning
- Mamyrzhan Sabyrzhan: Workout Session
- Bakhytzhan Azamat: History & Progress

## Run and validate

Open the project in Android Studio, sync Gradle, and run `app` on an API 24+ device.
The project is configured for compile/target SDK 37 and the Gradle daemon uses JDK 25.
Android Studio's bundled JBR can be used for Gradle.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-configuration-cache --console=plain
```

On Windows, a `GradleWorkerMain` class-not-found error can occur when the Gradle
user-home path contains Cyrillic characters. In that case, use an ASCII-only
`GRADLE_USER_HOME` path and let Gradle populate its cache there. This is a launcher
path-encoding issue; it also affects the original repository tests.

## Workout session module

Open Workout Plans, select a plan, and tap Start Workout. Enter actual weights and
repetitions, check completed sets, and add extra sets as needed. Uncheck a set to
correct its values. Previous shows the same set number from the latest finished
workout containing that exercise, across all plans.

Rest starts automatically after a completed set. Choose 60/90/120 seconds or a
custom duration from 1 to 3600 seconds; pause, resume, reset, or skip rest.
Back keeps the workout, drafts, and timer available through Resume Workout.
Finish Workout saves only checked sets, updates the dashboard, and makes the
results available in Workout History and exercise progress. Discard Workout
requires confirmation and saves nothing. Plans are never changed by set input.

Plans, exercise targets, and finished workouts are saved in Room (`fittrack.db`).
Sample plans are inserted once on the first database creation; deleting all plans
keeps the list empty on later launches. Theme preferences are saved with DataStore.
Active workout drafts and the rest timer stay in memory and survive rotation, but
are cleared after process death. A restored workout screen offers an explicit start
action. Finish waits for the session and its checked sets to commit together;
a failed write keeps the active workout available for retry.

See [WORKOUT_MODULE.md](WORKOUT_MODULE.md) for task branches and manual checks,
and [INTEGRATION.md](INTEGRATION.md) for shared module contracts.
See [TEAM_INTEGRATION.md](TEAM_INTEGRATION.md) for the shared Room and settings contracts.

With an Android device or emulator connected, run `:app:connectedDebugAndroidTest`
for the Room disk-reopen and screen checks. To verify a real app process restart,
first install the debug app and test APKs with `:app:installDebug :app:installDebugAndroidTest`,
then run:

```powershell
.\scripts\check-workout-restart.ps1 -Adb 'C:\path\to\AndroidSDK\platform-tools\adb.exe'
```
