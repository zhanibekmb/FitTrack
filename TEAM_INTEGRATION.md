# FitTrack team integration

## Ownership

| Area | Owner | Files |
| --- | --- | --- |
| Shared infrastructure and settings | Zhanibek Mukatay | Gradle files, `FitTrackContainer.kt`, `MainActivity.kt`, navigation, `ui/settings/`, `SettingsRepository.kt`, and the future `FitTrackDatabase.kt` |
| Workout planning and session persistence | Mamyrzhan Sabyrzhan | Plan entities/DAO/repository, `ui/plans/` persistence wiring, and `ui/workout/` Finish handoff |
| History and progress | Bakhytzhan Azamat | `ui/history/` and the future session/set entities, history DAO, and history repository, coordinated with the workout owner |
| Exercise catalog | Zhangazin Akniyet | Exercise API repository, Retrofit service, response models, and Coil UI integration |

This allocation follows the updated assignment: Sabyrzhan owns plan CRUD and
session persistence, Akniyet owns the REST exercise catalog.

## Room setup already in place

The app applies KSP 2.3.10 and Room 2.8.4. The shared version-1 database and its
exported schema are committed in `data/local/` and `app/schemas/`. Preferences
DataStore 1.2.1 saves appearance settings. `FitTrackContainer.database(context)`
caches the application-context database; `initialize(context)` connects it to the
shared repository. Exercises still come from the existing catalog pending REST integration.

## Database contract

1. Keep Room entities and DAOs in `data/local/`, and UI models in `data/model/`.
2. Use the existing `FitTrackDatabase`: `planDao()` and `historyDao()` share one file, `fittrack.db`.
3. `WorkoutPlanEntity` has ordered `PlannedExerciseEntity` children. The composite key is plan ID plus position, so repeated exercises retain independent targets. Plan upsert and child replacement are transactional.
4. The minimal `WorkoutSessionEntity`, `WorkoutSetEntity`, and `WorkoutHistoryDao` support the Finish handoff. Azamat can build his history repository and deletion UI on these shared tables. Keep the session name snapshot; do not cascade history when deleting a plan.
5. `WorkoutStorage` is the persistence boundary. `WorkoutPlanRepository` currently loads both plans and finished results so existing Home/History/Progress screens and Previous use restored data. Coordinate changes to this adapter when adding Azamat's repository; do not create a second database or parallel session models.
6. A `local_metadata` marker seeds demo plans once in a transaction. Never re-seed simply because the plan table is empty.
7. If a released schema changes, bump the version and provide and test a migration. Keep schema export enabled, main-thread queries disabled, and destructive fallback disabled.

## Connect teammate repositories

Keep the public `WorkoutData` flow shared across screens. Persistent methods on
`WorkoutRepository` are suspend functions; call them from ViewModel coroutines.
Workout set input, start, and discard remain sequential main-thread UI actions.
Catalog integration must preserve exercise IDs used by saved plans and results;
cache exercise metadata if remote entries need to remain readable offline.

Remaining team work includes the Retrofit/Coil catalog and Azamat's dedicated
history repository, history deletion, and any additional progress features.
Room tests cover disk reopen, plan CRUD, cascading exercise deletion, preserved
history, one-time seeding, and rollback of an invalid Finish transaction. The
restart script verifies persistence across separate app processes.
