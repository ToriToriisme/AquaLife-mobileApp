package com.example.aqualife.data.remote.payment

import com.example.aqualife.data.remote.payment.model.MomoPaymentRequest
import com.example.aqualife.data.remote.payment.model.MomoPaymentResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface MomoApiService {
    @POST("v2/gateway/api/create")
    suspend fun createPayment(@Body request: MomoPaymentRequest): MomoPaymentResponse
}

