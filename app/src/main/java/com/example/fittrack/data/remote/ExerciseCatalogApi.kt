package com.example.fittrack.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class WgerExerciseResponse(
    @SerializedName("results") val results: List<WgerExerciseDto> = emptyList()
)

data class WgerExerciseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val description: String?
)

data class RemoteExerciseItem(
    val id: Int,
    val name: String,
    val summary: String,
    val imageUrl: String? = null
)

sealed interface RemoteCatalogUiState {
    data object Loading : RemoteCatalogUiState
    data class Success(val items: List<RemoteExerciseItem>) : RemoteCatalogUiState
    data class Error(val message: String) : RemoteCatalogUiState
    data object Empty : RemoteCatalogUiState
}

interface WgerApiService {
    @GET("api/v2/exercise/")
    suspend fun getExercises(
        @Query("language") language: Int = 2,
        @Query("limit") limit: Int = 15
    ): WgerExerciseResponse
}

object RetrofitClientProvider {
    private const val BASE_URL = "https://wger.de/"

    val apiService: WgerApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WgerApiService::class.java)
    }
}