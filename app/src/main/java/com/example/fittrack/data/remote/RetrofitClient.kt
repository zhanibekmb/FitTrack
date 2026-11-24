package com.example.fittrack.data.remote

import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://wger.de/api/v2/"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(40, TimeUnit.SECONDS)
        .build()

    fun createExerciseApi(
        baseUrl: HttpUrl = BASE_URL.toHttpUrl(),
        client: OkHttpClient = httpClient
    ): ExerciseApiService {
        val gson = GsonBuilder().setStrictness(Strictness.STRICT).create()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ExerciseApiService::class.java)
    }
}
