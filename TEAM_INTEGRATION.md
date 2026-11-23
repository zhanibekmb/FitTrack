# FitTrack team integration

## Ownership

| Area | Owner | Files |
| --- | --- | --- |
| Shared infrastructure and settings | Zhanibek Mukatay | Gradle files, `FitTrackContainer.kt`, `MainActivity.kt`, navigation, `ui/settings/`, `SettingsRepository.kt`, and the future `FitTrackDatabase.kt` |
| Workout plans | Zhangazin Akniyet | `ui/plans/` and the future plan entities, DAO, and local repository |
| Workout session | Mamyrzhan Sabyrzhan | `ui/workout/` and the workout-to-history save handoff |
| History and progress | Bakhytzhan Azamat | `ui/history/` and the future session/set entities, history DAO, and history repository, coordinated with the workout owner |
| Exercise catalog | Catalog assignee | Future exercise API repository, Retrofit service, response models, and Coil UI integration |

The current README does not name a separate catalog owner. Confirm that assignment within the team.

## Room setup already in place

The app applies KSP 2.3.10 and declares Room runtime and compiler 2.8.4 in the version catalog. KSP will export Room schemas to `app/schemas/` once there is a database to process. Preferences DataStore 1.2.1 is used by Settings today. The existing workout repository still supplies plans, exercises, and history in memory.

## Add the database when the first real schema is ready

1. Put Room-specific Kotlin files in `app/src/main/java/com/example/fittrack/data/local/`. Keep the shared UI models in `data/model/`.
2. Add each real entity to `@Database(entities = [...], version = 1, exportSchema = true)`. Create `FitTrackDatabase` in that package and extend `RoomDatabase`.
3. Add one abstract getter per DAO. For example, add `abstract fun planDao(): WorkoutPlanDao` when that DAO exists. Do the same for history when its schema is ready.
4. In `FitTrackContainer`, cache one database instance in a synchronized getter, using the same pattern as `settingsRepository(context)`. Build it with the application context.

   ```kotlin
   val database = Room.databaseBuilder(
       context.applicationContext,
       FitTrackDatabase::class.java,
       "fittrack.db"
   ).build()
   ```

5. Commit the generated version-1 schema. If another feature changes a schema that is already shipped, bump the version and provide and test a migration. Use suspend DAO writes and Flow reads. Leave main-thread database access and destructive fallback disabled.

## Connect teammate repositories

Create each local repository in `data/repository/`. Give it the relevant DAO from the shared database. Expose one cached instance from `FitTrackContainer`. For example, the future plan repository can be constructed with `WorkoutPlanRepository(database(context).planDao())`. When a ViewModel needs an application context, use the `SettingsViewModel` pattern and call the matching container function with its `Application`. Keep network service creation behind the catalog repository and expose it through the same container.

Pending work includes the real plan and history schemas and queries, migration tests once a schema changes, the workout-to-history save handoff, and the Retrofit/Coil catalog. Add the local repositories as those features land and switch each UI to its new source in the same change that verifies it.
