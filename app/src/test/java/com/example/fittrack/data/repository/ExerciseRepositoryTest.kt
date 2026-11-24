package com.example.fittrack.data.repository

import com.example.fittrack.data.remote.RetrofitClient
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException

class ExerciseRepositoryTest {
    private val server = MockWebServer()
    private val api = RetrofitClient.createExerciseApi(server.url("/api/v2/"))
    private val repository = ExerciseRepository(api)

    @After
    fun stopServer() {
        server.shutdown()
    }

    @Test
    fun parsesWgerJsonAndSendsTheExpectedPublicRequest() = runBlocking {
        enqueue(page(benchPress, count = 16, nextOffset = 12))
        val dto = checkNotNull(api.getExercises(12, 0, "bench & press", muscleId = 4).body())

        assertEquals(16, dto.count)
        assertTrue(checkNotNull(dto.next).endsWith("offset=12"))
        assertNull(dto.previous)
        val exercise = checkNotNull(dto.results?.single())
        assertEquals(73, exercise.id)
        assertEquals("Chest", exercise.category?.name)
        assertEquals("<p>Example instructions.</p>", exercise.translations?.last()?.descriptionHtml)
        assertEquals("Pectoralis major", exercise.muscles?.single()?.name)
        assertEquals("Barbell", exercise.equipment?.first()?.name)
        assertEquals(true, exercise.images?.last()?.isMain)
        assertEquals(2, exercise.license?.id)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertNull(request.getHeader("Authorization"))
        val url = checkNotNull(request.requestUrl)
        assertEquals("/api/v2/exerciseinfo/", url.encodedPath)
        assertEquals("en", url.queryParameter("language__code"))
        assertEquals("id", url.queryParameter("ordering"))
        assertEquals("12", url.queryParameter("limit"))
        assertEquals("0", url.queryParameter("offset"))
        assertEquals("bench & press", url.queryParameter("name__search"))
        assertEquals("4", url.queryParameter("muscles"))
    }

    @Test
    fun mapsEnglishMetadataAndSeparatelyCreditsTheMainImage() = runBlocking {
        enqueue(page(benchPress))
        val exercise = repository.loadPage(" Bench ").exercises.single()

        assertEquals("wger:73", exercise.id)
        assertEquals("Bench Press", exercise.name)
        assertEquals("Chest", exercise.muscleGroup)
        assertEquals(listOf("Barbell"), exercise.equipment)
        assertEquals(listOf("Translation author"), exercise.credit.authors)
        assertEquals("CC BY 4.0", exercise.credit.license?.name)
        val image = checkNotNull(exercise.image)
        assertEquals("https://wger.de/media/main-thumb.png", image.url)
        assertEquals("https://wger.de/media/main.webp", image.credit.sourceUrl)
        assertEquals("Press diagram", image.credit.title)
        assertEquals(listOf("Illustrator", "Contributor"), image.credit.authors)
        assertEquals("https://creativecommons.org/licenses/by-sa/3.0/", image.credit.license?.url)
        assertEquals("Bench", server.takeRequest().requestUrl?.queryParameter("name__search"))
    }

    @Test
    fun skipsEntriesMissingEnglishAndHandlesOptionalFieldsAcrossPages() = runBlocking {
        val nonEnglish = """{"id":10,"translations":[{"language":1,"name":"Testübung"}]}"""
        val minimal = """
            {"id":11,"category":{"name":"Legs"},"translations":[null,{"language":2,"name":" Step-up "}]}
        """.trimIndent()
        enqueue(page(nonEnglish, count = 13, nextOffset = 12))
        enqueue(page(minimal, count = 13))
        val result = repository.loadPage("")

        val exercise = result.exercises.single()
        assertEquals("Step-up", exercise.name)
        assertEquals("Legs", exercise.muscleGroup)
        assertTrue(exercise.equipment.isEmpty())
        assertNull(exercise.image)
        assertNull(exercise.credit.license)
        assertNull(result.nextOffset)
        assertEquals("0", server.takeRequest().requestUrl?.queryParameter("offset"))
        assertEquals("12", server.takeRequest().requestUrl?.queryParameter("offset"))
    }

    @Test
    fun successfulEmptyPageHasNoExercisesOrContinuation() = runBlocking {
        enqueue(page("", count = 0))

        val result = repository.loadPage("no matching exercise")
        assertTrue(result.exercises.isEmpty())
        assertNull(result.nextOffset)
    }

    @Test
    fun readsMuscleFilterChoicesFromWger() = runBlocking {
        enqueue(page(
            """{"id":15,"name":"Soleus","name_en":""},{"id":4,"name":"Pectoralis","name_en":"Chest"}""",
            count = 2
        ))
        val muscles = repository.loadMuscles()

        assertEquals(listOf(4 to "Chest", 15 to "Soleus"), muscles.map { it.id to it.name })
        assertEquals("/api/v2/muscle/", server.takeRequest().requestUrl?.encodedPath)
    }

    @Test
    fun rejectsMalformedJsonEmptyBodyAndInvalidNextOffset() = runBlocking {
        enqueue("<html>Not JSON</html>")
        server.enqueue(MockResponse().setResponseCode(204))
        enqueue(page(benchPress, count = 16, nextOffset = 12))

        assertTrue(runCatching { repository.loadPage("") }.exceptionOrNull() is MalformedJsonException)
        assertTrue(runCatching { repository.loadPage("") }.exceptionOrNull() is InvalidExerciseResponse)
        assertTrue(runCatching { repository.loadPage("", offset = 12) }
            .exceptionOrNull() is InvalidExerciseResponse)
    }

    @Test
    fun propagatesHttpFailureInsteadOfReturningAnEmptyCatalog() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(503))
        val error = runCatching { repository.loadPage("") }.exceptionOrNull() as HttpException
        assertEquals(503, error.code())
    }

    private fun enqueue(body: String) {
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody(body))
    }

    private fun page(rows: String, count: Int = 1, nextOffset: Int? = null): String {
        val next = nextOffset?.let { "\"https://wger.de/api/v2/exerciseinfo/?offset=$it\"" } ?: "null"
        return """{"count":$count,"next":$next,"previous":null,"results":[$rows]}"""
    }

    private val benchPress = """
        {
          "id": 73,
          "category": {"id": 11, "name": "Chest"},
          "muscles": [{"id": 4, "name": "Pectoralis major", "name_en": "Chest"}],
          "equipment": [{"id": 1, "name": "Barbell"}, null, {"id": 2, "name": ""}],
          "license": {"id": 2, "short_name": "CC-BY-SA 4", "url": "https://creativecommons.org/licenses/by-sa/4.0/"},
          "license_author": "Exercise author",
          "translations": [
            {"language": 1, "name": "Bankdrücken"},
            {"language": 2, "name": "Bench Press", "description": "<p>Example instructions.</p>",
             "license": 4, "license_author": "Translation author"}
          ],
          "images": [
            {"image": "https://wger.de/media/other.png", "license": 2, "is_main": false},
            {"image": "https://wger.de/media/main.webp", "is_main": true, "license": 1,
             "thumbnails": {"small": "https://wger.de/media/main-thumb.png"},
             "license_title": "Press diagram", "license_author": "Illustrator",
             "author_history": ["Illustrator", "Contributor"]}
          ]
        }
    """.trimIndent()
}
