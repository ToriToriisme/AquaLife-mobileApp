package com.example.aqualife.utils

import java.net.URLEncoder
import java.util.Random

/**
 * Generator for MoMo QR Code with real bank transfer information
 * 
 * Format: MoMo QR code contains:
 * - Order ID (random)
 * - Amount
 * - Transfer info (phone number and name)
 */
object MomoQrGenerator {
    
    // Real MoMo account information
    private const val MOMO_PHONE = "0913133705"
    private const val MOMO_NAME = "Phan Thành Đức Nhân"
    
    // Real bank account information
    private const val BANK_ACCOUNT = "050120519889"
    private const val BANK_NAME = "Sacombank"
    private const val BANK_ACCOUNT_NAME = "Phan Thành Đức Nhân"
    
    /**
     * Generate random order ID for MoMo payment
     * Format: MOMO + timestamp + random 4 digits
     */
    fun generateOrderId(): String {
        val timestamp = System.currentTimeMillis()
        val random = Random().nextInt(1000, 9999)
        return "MOMO${timestamp}${random}"
    }
    
    /**
     * Generate MoMo QR code data string
     * Format compatible with MoMo app scanning
     */
    fun generateMomoQrData(amount: Long, orderId: String): String {
        // MoMo QR format: 2|99|phone|name|amount|orderId|description
        val description = "Thanh toan don hang AquaLife - Ma don: $orderId"
        return "2|99|$MOMO_PHONE|$MOMO_NAME|$amount|$orderId|$description"
    }
    
    /**
     * Generate QR code image URL from MoMo data
     */
    fun generateMomoQrUrl(amount: Long, orderId: String): String {
        val qrData = generateMomoQrData(amount, orderId)
        val encoded = try {
            URLEncoder.encode(qrData, "UTF-8")
        } catch (e: Exception) {
            qrData
        }
        return "https://api.qrserver.com/v1/create-qr-code/?size=480x480&data=$encoded"
    }
    
    /**
     * Generate bank transfer QR code data
     * Format: STK + amount + content
     */
    fun generateBankQrData(amount: Long, orderId: String): String {
        val content = "AQUALIFE $orderId"
        return "$BANK_ACCOUNT|$amount|$content"
    }
    
    /**
     * Generate bank transfer QR code URL
     */
    fun generateBankQrUrl(amount: Long, orderId: String): String {
        val qrData = generateBankQrData(amount, orderId)
        val encoded = try {
            URLEncoder.encode(qrData, "UTF-8")
        } catch (e: Exception) {
            qrData
        }
        return "https://api.qrserver.com/v1/create-qr-code/?size=480x480&data=$encoded"
    }
    
    /**
     * Get bank transfer information for display
     */
    fun getBankTransferInfo(): BankTransferInfo {
        return BankTransferInfo(
            accountNumber = BANK_ACCOUNT,
            bankName = BANK_NAME,
            accountName = BANK_ACCOUNT_NAME
        )
    }
    
    /**
     * Get MoMo transfer information for display
     */
    fun getMomoTransferInfo(): MomoTransferInfo {
        return MomoTransferInfo(
            phone = MOMO_PHONE,
            name = MOMO_NAME
        )
    }
    
    data class BankTransferInfo(
        val accountNumber: String,
        val bankName: String,
        val accountName: String
    )
    
    data class MomoTransferInfo(
        val phone: String,
        val name: String
    )
}

