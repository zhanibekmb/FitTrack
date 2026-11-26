package com.example.fittrack

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fittrack.data.local.FitTrackDatabase
import com.example.fittrack.data.model.CatalogCredit
import com.example.fittrack.data.model.CatalogExercise
import com.example.fittrack.data.repository.WorkoutPlanRepository
import com.example.fittrack.data.repository.WorkoutRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class WorkoutCatalogPersistenceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "catalog-${UUID.randomUUID()}.db"
    private var database: FitTrackDatabase? = null
    private fun open() = Room.databaseBuilder(context, FitTrackDatabase::class.java, name)
        .addMigrations(FitTrackDatabase.MIGRATION_1_2).build().also { database = it }

    @After fun cleanUp() { database?.close(); context.deleteDatabase(name) }

    @Test fun importedExerciseMetadataSurvivesPlanDeletionAndReopen() = runBlocking {
        val db = open()
        val repository = WorkoutRepository(WorkoutPlanRepository(db))
        val observer = launch { repository.observeStorage() }
        withTimeout(10_000) { repository.data.first { !it.isLoading } }
        val plan = repository.createPlan("API plan", 3)
        val exercise = CatalogExercise(73, "Bench Press", listOf("Chest"), "Chest",
            listOf("Barbell", "Bench"), CatalogCredit("Bench", emptyList(), "https://wger.de/", null), null)
        assertTrue(repository.addCatalogExerciseToPlan(plan.id, exercise, 3, 8, 35.5))
        assertNotNull(repository.startWorkout(plan.id))
        assertTrue(repository.completeSet(exercise.id, 1, 35.5, 8))
        val finished = repository.finishWorkout()!!
        assertTrue(repository.deletePlan(plan.id))
        observer.cancelAndJoin()
        db.close()
        val saved = WorkoutPlanRepository(open()).read()
        val restored = saved.exercises.single { it.id == exercise.id }
        assertEquals(exercise.name, restored.name)
        assertEquals("Chest", restored.muscleGroup)
        assertEquals(exercise.equipment, restored.equipment)
        assertEquals(finished, saved.sessions.single())
        assertFalse(saved.plans.any { it.id == plan.id })
    }

    @Test fun migrationFromVersionOnePreservesPlansAndFinishedSets() = runBlocking {
        val schema = InstrumentationRegistry.getInstrumentation().context.assets
            .open("com.example.fittrack.data.local.FitTrackDatabase/1.json")
            .bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { old ->
            val entities = schema.getJSONArray("entities")
            for (index in 0 until entities.length()) {
                val entity = entities.getJSONObject(index)
                val table = entity.getString("tableName")
                old.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                val indices = entity.optJSONArray("indices") ?: org.json.JSONArray()
                for (position in 0 until indices.length()) {
                    old.execSQL(indices.getJSONObject(position).getString("createSql").replace("\${TABLE_NAME}", table))
                }
            }
            val setup = schema.getJSONArray("setupQueries")
            for (index in 0 until setup.length()) old.execSQL(setup.getString(index))
            old.execSQL("INSERT INTO local_metadata (`key`) VALUES ('plans_seeded')")
            old.execSQL("INSERT INTO workout_plans VALUES ('old-plan', 'Saved in v1', 2, 0)")
            old.execSQL("INSERT INTO planned_exercises VALUES ('old-plan', 0, 'db-bench', 3, 8, 25.0)")
            old.execSQL("INSERT INTO workout_sessions VALUES ('old-session', 'old-plan', 'Saved in v1', 100, 200)")
            old.execSQL("INSERT INTO workout_sets VALUES ('old-session', 'db-bench', 1, 25.0, 8, 0)")
            old.version = 1
        }
        val local = WorkoutPlanRepository(open())
        local.initialize(com.example.fittrack.data.DemoData.plans)
        val saved = local.read()
        assertEquals("Saved in v1", saved.plans.single().name)
        assertEquals(25.0, saved.plans.single().exercises.single().targetWeightKg, 0.0)
        assertEquals(200.0, saved.sessions.single().totalVolumeKg(), 0.0)
        assertTrue(database!!.planDao().getCatalogExercises().isEmpty())
    }
}
