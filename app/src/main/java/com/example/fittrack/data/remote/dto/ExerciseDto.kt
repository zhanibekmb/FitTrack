package com.example.fittrack.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ExerciseDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("category") val category: NamedDto?,
    @SerializedName("muscles") val muscles: List<MuscleDto?>?,
    @SerializedName("equipment") val equipment: List<NamedDto?>?,
    @SerializedName("translations") val translations: List<TranslationDto?>?,
    @SerializedName("images") val images: List<ExerciseImageDto?>?,
    @SerializedName("license") val license: LicenseDto?,
    @SerializedName("license_author") val licenseAuthor: String?
)

data class NamedDto(
    @SerializedName("name") val name: String?
)

data class MuscleDto(
    @SerializedName("name") val name: String?,
    @SerializedName("name_en") val englishName: String?
)

data class TranslationDto(
    @SerializedName("language") val language: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val descriptionHtml: String?,
    @SerializedName("license") val licenseId: Int?,
    @SerializedName("license_author") val licenseAuthor: String?,
    @SerializedName("author_history") val authorHistory: List<String?>?
)

data class ExerciseImageDto(
    @SerializedName("image") val image: String?,
    @SerializedName("thumbnails") val thumbnails: ThumbnailsDto?,
    @SerializedName("is_main") val isMain: Boolean?,
    @SerializedName("license") val licenseId: Int?,
    @SerializedName("license_title") val licenseTitle: String?,
    @SerializedName("license_author") val licenseAuthor: String?,
    @SerializedName("author_history") val authorHistory: List<String?>?
)

data class ThumbnailsDto(
    @SerializedName("small") val small: String?
)

data class LicenseDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("short_name") val shortName: String?,
    @SerializedName("url") val url: String?
)
