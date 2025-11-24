package com.example.aqualife.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aqualife.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Processing : PaymentUiState()
    data class Ready(
        val method: PaymentMethod,
        val payUrl: String?,
        val qrUrl: String?,
        val expireAt: Long
    ) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun initiatePayment(
        name: String,
        phone: String,
        email: String,
        amount: Long,
        method: PaymentMethod
    ) {
        if (name.isBlank() || phone.isBlank() || email.isBlank()) {
            _uiState.value = PaymentUiState.Error("Vui lòng nhập đủ thông tin người nhận.")
            return
        }

        viewModelScope.launch {
            _uiState.value = PaymentUiState.Processing
            try {
                when (method) {
                    PaymentMethod.MOMO -> handleMomoPayment(name, phone, email, amount)
                    PaymentMethod.BANK -> _uiState.value = PaymentUiState.Error("Phương thức ngân hàng đang được cập nhật.")
                    PaymentMethod.VNPAY -> handleVnPayPayment(amount)
                }
            } catch (e: Exception) {
                _uiState.value = PaymentUiState.Error(e.message ?: "Thanh toán thất bại")
            }
        }
    }

    private suspend fun handleMomoPayment(
        name: String,
        phone: String,
        email: String,
        amount: Long
    ) {
        val response = paymentRepository.createMomoPayment(
            amount = amount,
            orderInfo = "Thanh toán đơn hàng AquaLife",
            customerName = name,
            phone = phone,
            email = email,
            requestType = "captureWallet"
        )
        val payUrl = response.payUrl
        val qrUrl = response.qrCodeUrl ?: payUrl?.let { buildQrFromUrl(it) }
        if (payUrl != null) {
            val expireAt = System.currentTimeMillis() + QR_EXPIRE_DURATION
            _uiState.value = PaymentUiState.Ready(
                method = PaymentMethod.MOMO,
                payUrl = payUrl,
                qrUrl = qrUrl,
                expireAt = expireAt
            )
        } else {
            _uiState.value = PaymentUiState.Error(response.message ?: "Không thể tạo thanh toán")
        }
    }

    private suspend fun handleVnPayPayment(amount: Long) {
        val result = paymentRepository.createVnPayPayment(
            amount = amount,
            orderInfo = "Thanh toán đơn hàng AquaLife"
        )
        _uiState.value = PaymentUiState.Ready(
            method = PaymentMethod.VNPAY,
            payUrl = result.payUrl,
            qrUrl = buildQrFromUrl(result.payUrl),
            expireAt = result.expireAt
        )
    }

    private fun buildQrFromUrl(url: String): String {
        val encoded = try {
            URLEncoder.encode(url, "UTF-8")
        } catch (_: Exception) {
            url
        }
        return "https://api.qrserver.com/v1/create-qr-code/?size=480x480&data=$encoded"
    }

    fun resetState() {
        _uiState.value = PaymentUiState.Idle
    }

    companion object {
        private const val QR_EXPIRE_DURATION = 10 * 60 * 1000L
    }
}

