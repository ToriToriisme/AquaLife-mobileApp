package com.example.aqualife.data.remote

import com.example.aqualife.data.remote.dto.FishDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface AquaLifeApiService {
    @GET("fish")
    suspend fun getAllFish(): Response<List<FishDto>>

    @GET("fish/{id}")
    suspend fun getFishById(@Path("id") id: String): Response<FishDto>

    @GET("fish/category/{category}")
    suspend fun getFishByCategory(@Path("category") category: String): Response<List<FishDto>>
}

