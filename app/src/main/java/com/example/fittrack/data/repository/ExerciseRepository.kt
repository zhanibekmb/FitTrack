package com.example.fittrack.data.repository

import com.example.fittrack.data.model.CatalogCredit
import com.example.fittrack.data.model.CatalogExercise
import com.example.fittrack.data.model.CatalogImage
import com.example.fittrack.data.model.CatalogLicense
import com.example.fittrack.data.remote.ExerciseApiService
import com.example.fittrack.data.remote.dto.ExerciseDto
import com.example.fittrack.data.remote.dto.LicenseDto
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.HttpException

data class ExerciseCatalogPage(val exercises: List<CatalogExercise>, val nextOffset: Int?)

class InvalidExerciseResponse : Exception("Invalid wger exercise response")

class ExerciseRepository(private val api: ExerciseApiService) {
    suspend fun loadPage(query: String, offset: Int = 0): ExerciseCatalogPage {
        var pageOffset = offset
        do {
            val response = api.getExercises(PAGE_SIZE, pageOffset, query.trim().ifEmpty { null })
            if (!response.isSuccessful) {
                response.errorBody()?.close()
                throw HttpException(response)
            }
            val page = response.body() ?: throw InvalidExerciseResponse()
            val count = page.count?.takeIf { it >= 0 } ?: throw InvalidExerciseResponse()
            val results = page.results ?: throw InvalidExerciseResponse()
            val nextOffset = page.next?.let { url ->
                // Keep using the configured Retrofit endpoint and take only the offset from wger.
                url.toHttpUrlOrNull()?.queryParameter("offset")?.toIntOrNull()
                    ?.takeIf { it > pageOffset && it < count && results.isNotEmpty() }
                    ?: throw InvalidExerciseResponse()
            }
            val exercises = results.filterNotNull().mapNotNull { it.toCatalogExercise() }
                .distinctBy { it.wgerId }

            if (exercises.isNotEmpty() || nextOffset == null) {
                return ExerciseCatalogPage(exercises, nextOffset)
            }
            pageOffset = nextOffset
        } while (true)
    }

    private fun ExerciseDto.toCatalogExercise(): CatalogExercise? {
        val id = id?.takeIf { it > 0 } ?: throw InvalidExerciseResponse()
        val english = translations.orEmpty().filterNotNull().firstOrNull {
            it.language == ENGLISH && !it.name.isNullOrBlank()
        } ?: return null
        val name = english.name.clean() ?: return null
        val source = "https://wger.de/en/exercise/$id/view/"
        val image = images.orEmpty().filterNotNull().sortedByDescending { it.isMain == true }
            .firstNotNullOfOrNull { candidate ->
                val original = candidate.image.wgerImageUrl() ?: return@firstNotNullOfOrNull null
                val imageLicense = licenseFor(candidate.licenseId, license)
                    ?: return@firstNotNullOfOrNull null
                CatalogImage(
                    url = candidate.thumbnails?.small.wgerImageUrl() ?: original,
                    credit = CatalogCredit(
                        title = candidate.licenseTitle.clean() ?: name,
                        authors = authors(candidate.licenseAuthor, candidate.authorHistory),
                        sourceUrl = original,
                        license = imageLicense
                    )
                )
            }
        return CatalogExercise(
            wgerId = id,
            name = name,
            muscles = muscles.orEmpty().filterNotNull()
                .mapNotNull { it.englishName.clean() ?: it.name.clean() }.distinct(),
            category = category?.name.clean(),
            equipment = equipment.orEmpty().mapNotNull { it?.name.clean() }.distinct(),
            credit = CatalogCredit(
                title = name,
                authors = authors(english.licenseAuthor, english.authorHistory)
                    .ifEmpty { listOfNotNull(licenseAuthor.clean()) },
                sourceUrl = source,
                license = licenseFor(english.licenseId ?: license?.id, license)
            ),
            image = image
        )
    }

    private fun licenseFor(id: Int?, exerciseLicense: LicenseDto?): CatalogLicense? {
        if (id == null) return null
        if (id == exerciseLicense?.id) {
            val name = exerciseLicense.shortName.clean()
            val url = exerciseLicense.url?.toHttpUrlOrNull()?.takeIf { it.isHttps }
            if (name != null && url != null) return CatalogLicense(name, url.toString())
        }
        return wgerLicenses[id]
    }

    private fun authors(primary: String?, history: List<String?>?): List<String> =
        (listOf(primary) + history.orEmpty()).mapNotNull { it.clean() }.distinct()

    private fun String?.clean(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private fun String?.wgerImageUrl(): String? = this?.toHttpUrlOrNull()
        ?.takeIf { it.isHttps && it.host == "wger.de" }?.toString()

    private companion object {
        const val PAGE_SIZE = 12
        const val ENGLISH = 2

        // The translation and image serializers return license IDs, which can differ from the exercise.
        val wgerLicenses = mapOf(
            1 to CatalogLicense("CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0/"),
            2 to CatalogLicense("CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/"),
            3 to CatalogLicense("CC0 1.0", "https://creativecommons.org/publicdomain/zero/1.0/"),
            4 to CatalogLicense("CC BY 4.0", "https://creativecommons.org/licenses/by/4.0/"),
            5 to CatalogLicense("ODbL", "https://opendatacommons.org/licenses/odbl/")
        )
    }
}
