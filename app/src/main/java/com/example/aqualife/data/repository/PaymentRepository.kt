package com.example.aqualife.data.repository

import android.util.Base64
import com.example.aqualife.data.remote.payment.MomoApiService
import com.example.aqualife.data.remote.payment.model.MomoPaymentRequest
import com.example.aqualife.data.remote.payment.model.MomoPaymentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.TreeMap
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val momoApiService: MomoApiService
) {

    suspend fun createMomoPayment(
        amount: Long,
        orderInfo: String,
        customerName: String,
        phone: String,
        email: String,
        requestType: String = "captureWallet"
    ): MomoPaymentResponse = withContext(Dispatchers.IO) {
        val orderId = System.currentTimeMillis().toString()
        val requestId = orderId
        val extraData = buildExtraData(customerName, phone, email)

        val rawHash = buildSignatureRaw(
            amount = amount,
            orderId = orderId,
            orderInfo = orderInfo,
            requestId = requestId,
            extraData = extraData,
            requestType = requestType
        )
        val signature = hmacSHA256(rawHash, PaymentConfig.secretKey)

        val request = MomoPaymentRequest(
            partnerCode = PaymentConfig.partnerCode,
            partnerName = "AquaLife",
            storeId = "AquaLifeStore",
            requestId = requestId,
            amount = amount.toString(),
            orderId = orderId,
            orderInfo = orderInfo,
            redirectUrl = PaymentConfig.redirectUrl,
            ipnUrl = PaymentConfig.ipnUrl,
            lang = "vi",
            extraData = extraData,
            requestType = requestType,
            signature = signature
        )

        momoApiService.createPayment(request)
    }

    suspend fun createVnPayPayment(
        amount: Long,
        orderInfo: String
    ): VnPayPaymentResult = withContext(Dispatchers.IO) {
        val formatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Etc/GMT+7")
        }
        val now = Date()
        val createDate = formatter.format(now)
        val expireAt = System.currentTimeMillis() + VnPayConfig.expireAfterMillis
        val expireDate = formatter.format(Date(expireAt))
        val txnRef = System.currentTimeMillis().toString()

        val params = TreeMap<String, String>()
        params["vnp_Version"] = "2.1.0"
        params["vnp_Command"] = "pay"
        params["vnp_TmnCode"] = VnPayConfig.tmnCode
        params["vnp_Amount"] = (amount * 100).toString()
        params["vnp_CurrCode"] = "VND"
        params["vnp_TxnRef"] = txnRef
        params["vnp_OrderInfo"] = orderInfo
        params["vnp_OrderType"] = "billpayment"
        params["vnp_Locale"] = "vn"
        params["vnp_ReturnUrl"] = VnPayConfig.returnUrl
        params["vnp_IpAddr"] = "0.0.0.0"
        params["vnp_CreateDate"] = createDate
        params["vnp_ExpireDate"] = expireDate

        val hashData = params.entries.joinToString("&") { "${it.key}=${it.value}" }
        val query = params.entries.joinToString("&") { "${it.key}=${urlEncode(it.value)}" }
        val secureHash = hmacSHA512(hashData, VnPayConfig.hashSecret)
        val payUrl = "${VnPayConfig.baseUrl}?$query&vnp_SecureHash=$secureHash"

        VnPayPaymentResult(payUrl = payUrl, expireAt = expireAt)
    }

    private fun buildExtraData(name: String, phone: String, email: String): String {
        val raw = "name=${urlEncode(name)}&phone=${urlEncode(phone)}&email=${urlEncode(email)}"
        return Base64.encodeToString(raw.toByteArray(), Base64.NO_WRAP)
    }

    private fun buildSignatureRaw(
        amount: Long,
        orderId: String,
        orderInfo: String,
        requestId: String,
        extraData: String,
        requestType: String
    ): String {
        return "accessKey=${PaymentConfig.accessKey}" +
            "&amount=$amount" +
            "&extraData=$extraData" +
            "&ipnUrl=${PaymentConfig.ipnUrl}" +
            "&orderId=$orderId" +
            "&orderInfo=$orderInfo" +
            "&partnerCode=${PaymentConfig.partnerCode}" +
            "&redirectUrl=${PaymentConfig.redirectUrl}" +
            "&requestId=$requestId" +
            "&requestType=$requestType"
    }

    private fun hmacSHA256(data: String, secret: String): String {
        val algorithm = "HmacSHA256"
        val mac = Mac.getInstance(algorithm)
        val secretKey = SecretKeySpec(secret.toByteArray(), algorithm)
        mac.init(secretKey)
        val hash = mac.doFinal(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun urlEncode(value: String): String {
        return try {
            URLEncoder.encode(value, "UTF-8")
        } catch (_: Exception) {
            value
        }
    }

    private fun hmacSHA512(data: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA512")
        val secretKey = SecretKeySpec(secret.toByteArray(), "HmacSHA512")
        mac.init(secretKey)
        val hash = mac.doFinal(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}

object PaymentConfig {
    const val partnerCode = "MOMO4MUD20240115_TEST"
    const val accessKey = "Ekj9og2VnRfOuIys"
    const val secretKey = "PseUbm2s8QVJEbexsh8H3Jz2qa9tDqoa"
    const val redirectUrl = "https://forhershop.vn"
    const val ipnUrl = "https://forhershop.vn/shop"
}

object VnPayConfig {
    const val tmnCode = "2QXUI4J4"
    const val hashSecret = "KXEONBOZSHDR0NE5111M9Z5V8EG1N7GO"
    const val baseUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"
    const val returnUrl = "https://aqualife.vn/payment-return"
    const val expireAfterMillis = 10 * 60 * 1000L
}

data class VnPayPaymentResult(
    val payUrl: String,
    val expireAt: Long
)

