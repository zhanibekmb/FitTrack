package com.example.fittrack.data.remote

import com.example.fittrack.data.remote.dto.ExerciseDto
import com.example.fittrack.data.remote.dto.MuscleDto
import com.example.fittrack.data.remote.dto.PaginatedResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ExerciseApiService {
    @Headers("Accept: application/json")
    @GET("exerciseinfo/?language__code=en&ordering=id")
    suspend fun getExercises(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("name__search") query: String?,
        @Query("muscles") muscleId: Int? = null
    ): Response<PaginatedResponse<ExerciseDto>>

    @Headers("Accept: application/json")
    @GET("muscle/?limit=50")
    suspend fun getMuscles(): Response<PaginatedResponse<MuscleDto>>
}
