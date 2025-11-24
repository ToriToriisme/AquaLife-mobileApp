package com.example.aqualife.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FishDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("priceInt") val priceInt: Int,
    @SerializedName("category") val category: String,
    @SerializedName("habitat") val habitat: String,
    @SerializedName("maxWeight") val maxWeight: String,
    @SerializedName("diet") val diet: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("description") val description: String = "",
    @SerializedName("lastUpdated") val lastUpdated: Long = System.currentTimeMillis()
)

