package com.example.aqualife.data.remote.payment.model

import com.google.gson.annotations.SerializedName

data class MomoPaymentRequest(
    @SerializedName("partnerCode") val partnerCode: String,
    @SerializedName("partnerName") val partnerName: String,
    @SerializedName("storeId") val storeId: String,
    @SerializedName("requestId") val requestId: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("orderId") val orderId: String,
    @SerializedName("orderInfo") val orderInfo: String,
    @SerializedName("redirectUrl") val redirectUrl: String,
    @SerializedName("ipnUrl") val ipnUrl: String,
    @SerializedName("lang") val lang: String,
    @SerializedName("extraData") val extraData: String,
    @SerializedName("requestType") val requestType: String,
    @SerializedName("autoCapture") val autoCapture: Boolean = true,
    @SerializedName("signature") val signature: String
)

data class MomoPaymentResponse(
    @SerializedName("resultCode") val resultCode: Int?,
    @SerializedName("message") val message: String?,
    @SerializedName("payUrl") val payUrl: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("qrCodeUrl") val qrCodeUrl: String?
)

