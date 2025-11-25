package com.example.aqualife.ui.viewmodel

// ============================================================================
// ANDROIDX IMPORTS
// ============================================================================
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

// ============================================================================
// THIRD-PARTY IMPORTS
// ============================================================================
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

// ============================================================================
// LOCAL IMPORTS
// ============================================================================
import com.example.aqualife.data.repository.PaymentRepository

// ============================================================================
// PAYMENT VIEWMODEL
// ============================================================================

enum class PaymentMethod(val displayName: String, val icon: ImageVector) {
    MOMO("MoMo", Icons.Default.AccountBalanceWallet),
    VNPAY("VNPay QR", Icons.Default.QrCodeScanner),
    BANK("Ngân hàng", Icons.Default.AccountBalance)
}

sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Processing : PaymentUiState()
    data class Ready(
        val method: PaymentMethod,
        val payUrl: String?,
        val qrUrl: String?,
        val expireAt: Long,
        val orderId: String? = null
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
        try {
            // Generate real MoMo QR code with transfer info
            val orderId = com.example.aqualife.utils.MomoQrGenerator.generateOrderId()
            val qrUrl = com.example.aqualife.utils.MomoQrGenerator.generateMomoQrUrl(amount, orderId)
            val expireAt = System.currentTimeMillis() + QR_EXPIRE_DURATION
            
            // Create a payment URL (can be used to open MoMo app)
            // Format: momo://transfer?phone=0913133705&amount=150000&note=AQUALIFE%20MOMO1234567890
            val payUrl = "momo://transfer?phone=0913133705&amount=$amount&note=AQUALIFE%20$orderId"
            
            // Set ready state with QR code
            _uiState.value = PaymentUiState.Ready(
                method = PaymentMethod.MOMO,
                payUrl = payUrl,
                qrUrl = qrUrl,
                expireAt = expireAt,
                orderId = orderId
            )
        } catch (e: Exception) {
            _uiState.value = PaymentUiState.Error("Lỗi tạo mã QR: ${e.message ?: "Không xác định"}")
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
