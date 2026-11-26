package com.example.fittrack.ui.exercises

import com.example.fittrack.data.remote.ExerciseApiService
import com.example.fittrack.data.remote.dto.ExerciseDto
import com.example.fittrack.data.remote.dto.MuscleDto
import com.example.fittrack.data.remote.dto.PaginatedResponse
import com.example.fittrack.data.remote.dto.TranslationDto
import com.example.fittrack.data.repository.ExerciseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseViewModelTest {
    @Test
    fun initialNetworkFailureCanRetryToEmptyWithoutDuplicateRequests() = catalogTest {
        var offline = true
        val api = FakeApi {
            if (offline) throw IOException("No network")
            page()
        }
        val model = ExerciseViewModel(ExerciseRepository(api))
        assertEquals(ExerciseCatalogContent.Loading, model.state.value.content)

        runCurrent()
        assertEquals(ExerciseCatalogContent.Error(ExerciseLoadProblem.NETWORK), model.state.value.content)

        offline = false
        model.retry()
        model.retry()
        assertEquals(ExerciseCatalogContent.Loading, model.state.value.content)
        runCurrent()
        assertEquals(ExerciseCatalogContent.Empty, model.state.value.content)
        assertEquals(listOf(0, 0), api.requests.map { it.offset })
    }

    @Test
    fun paginationKeepsRowsAndRetriesTheSamePageAfterTimeout() = catalogTest {
        var failNextPage = true
        val api = FakeApi { request ->
            when {
                request.offset == 0 -> page(exercise(73, "Bench Press"), nextOffset = 12)
                failNextPage -> throw SocketTimeoutException("Timed out")
                else -> page(exercise(73, "Bench Press"), exercise(75, "Dumbbell Press"))
            }
        }
        val model = ExerciseViewModel(ExerciseRepository(api))
        runCurrent()
        assertTrue(model.success.hasMore)

        model.loadMore()
        model.loadMore()
        assertTrue(model.success.loadingMore)
        runCurrent()
        assertEquals(listOf("wger:73"), model.success.exercises.map { it.id })
        assertEquals(ExerciseLoadProblem.TIMEOUT, model.success.pageError)
        model.loadMore()
        assertFalse(model.success.loadingMore)

        failNextPage = false
        model.retry()
        model.retry()
        runCurrent()
        assertEquals(listOf("wger:73", "wger:75"), model.success.exercises.map { it.id })
        assertFalse(model.success.hasMore)
        assertEquals(listOf(0, 12, 12), api.requests.map { it.offset })
    }

    @Test
    fun searchCancelsOlderRequestsAndMuscleChangesRestartAtFirstPage() = catalogTest {
        var initialCancelled = false
        val api = FakeApi { request ->
            if (request.query == null) {
                try {
                    awaitCancellation()
                } finally {
                    initialCancelled = true
                }
            }
            page(exercise(73, "Bench Press"))
        }
        val model = ExerciseViewModel(ExerciseRepository(api))
        runCurrent()
        assertEquals("Chest", model.state.value.muscles.single().name)

        model.search("ben")
        runCurrent()
        advanceTimeBy(200)
        model.search("bench")
        runCurrent()
        assertTrue(initialCancelled)
        advanceTimeBy(350)
        runCurrent()
        assertEquals("Bench Press", model.success.exercises.single().name)
        assertEquals(listOf(null, "bench"), api.requests.map { it.query })

        model.search(" bench ")
        model.selectMuscle(4)
        runCurrent()
        assertEquals(Request(0, "bench", 4), api.requests.last())
        assertEquals(3, api.requests.size)
    }

    private val ExerciseViewModel.success: ExerciseCatalogContent.Success
        get() = state.value.content as ExerciseCatalogContent.Success

    private fun catalogTest(block: suspend TestScope.() -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private data class Request(val offset: Int, val query: String?, val muscleId: Int?)

    private class FakeApi(
        private val response: suspend (Request) -> Response<PaginatedResponse<ExerciseDto>>
    ) : ExerciseApiService {
        val requests = mutableListOf<Request>()

        override suspend fun getExercises(
            limit: Int,
            offset: Int,
            query: String?,
            muscleId: Int?
        ): Response<PaginatedResponse<ExerciseDto>> {
            val request = Request(offset, query, muscleId)
            requests += request
            return response(request)
        }

        override suspend fun getMuscles(): Response<PaginatedResponse<MuscleDto>> =
            Response.success(PaginatedResponse(1, null, null, listOf(MuscleDto(4, "Pectoralis", "Chest"))))
    }

    private fun page(vararg exercises: ExerciseDto, nextOffset: Int? = null) =
        Response.success(
            PaginatedResponse(
                count = nextOffset?.plus(1) ?: exercises.size,
                next = nextOffset?.let { "https://wger.de/api/v2/exerciseinfo/?offset=$it" },
                previous = null,
                results = exercises.toList()
            )
        )

    private fun exercise(id: Int, name: String) = ExerciseDto(
        id = id,
        category = null,
        muscles = null,
        equipment = null,
        translations = listOf(TranslationDto(2, name, null, null, null, null)),
        images = null,
        license = null,
        licenseAuthor = null
    )
}
