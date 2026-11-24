package com.example.fittrack.data.model

data class CatalogExercise(
    val wgerId: Int,
    val name: String,
    val muscles: List<String>,
    val category: String?,
    val equipment: List<String>,
    val credit: CatalogCredit,
    val image: CatalogImage?
) {
    val id: String get() = "wger:$wgerId"
    val muscleGroup: String?
        get() = muscles.takeIf { it.isNotEmpty() }?.joinToString() ?: category

    internal fun toWorkoutExercise() = Exercise(id, name, muscleGroup.orEmpty(), equipment)
}

data class CatalogImage(
    val url: String,
    val credit: CatalogCredit
)

data class CatalogCredit(
    val title: String,
    val authors: List<String>,
    val sourceUrl: String,
    val license: CatalogLicense?
)

data class CatalogLicense(val name: String, val url: String)

data class CatalogMuscle(val id: Int, val name: String)
