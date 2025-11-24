package com.example.aqualife.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aqualife.ui.viewmodel.CartViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    cartViewModel: CartViewModel = hiltViewModel()
) {
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var paymentStatus by remember { mutableStateOf<PaymentStatus?>(null) }

    val totalPrice by cartViewModel.totalPrice.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thanh Toán", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Order Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tóm tắt đơn hàng", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tổng cộng:", fontSize = 16.sp)
                        Text(
                            formatCurrency(totalPrice.toInt()),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Red
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Chọn phương thức thanh toán", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // Payment Methods
            PaymentMethodCard(
                method = PaymentMethod.MOMO,
                isSelected = selectedPaymentMethod == PaymentMethod.MOMO,
                onClick = { selectedPaymentMethod = PaymentMethod.MOMO }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PaymentMethodCard(
                method = PaymentMethod.BANK,
                isSelected = selectedPaymentMethod == PaymentMethod.BANK,
                onClick = { selectedPaymentMethod = PaymentMethod.BANK }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Pay Button
            Button(
                onClick = {
                    if (selectedPaymentMethod != null) {
                        showPaymentDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedPaymentMethod != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Thanh Toán", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }

    // Payment Processing Dialog
    if (showPaymentDialog) {
        PaymentProcessingDialog(
            paymentMethod = selectedPaymentMethod!!,
            totalAmount = totalPrice,
            onDismiss = { showPaymentDialog = false },
            onSuccess = { transactionCode ->
                paymentStatus = PaymentStatus.Success(transactionCode)
                showPaymentDialog = false
            },
            onFailure = {
                paymentStatus = PaymentStatus.Failed
                showPaymentDialog = false
            }
        )
    }

    // Payment Result Dialog
    paymentStatus?.let { status ->
        PaymentResultDialog(
            status = status,
            onDismiss = {
                paymentStatus = null
                if (status is PaymentStatus.Success) {
                    cartViewModel.clearCart()
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            }
        )
    }
}

enum class PaymentMethod(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    MOMO("MoMo", Icons.Default.AccountBalanceWallet),
    BANK("Ngân hàng", Icons.Default.AccountBalance)
}

sealed class PaymentStatus {
    data class Success(val transactionCode: String) : PaymentStatus()
    object Failed : PaymentStatus()
}

@Composable
fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = method.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = method.displayName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PaymentProcessingDialog(
    paymentMethod: PaymentMethod,
    totalAmount: Double,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit,
    onFailure: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        // Simulate payment processing
        repeat(100) {
            delay(20)
            progress = (it + 1) / 100f
        }
        delay(500)
        isLoading = false
        
        // Generate random transaction code
        val transactionCode = "TRX_${System.currentTimeMillis()}"
        onSuccess(transactionCode)
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Đang xử lý thanh toán...") },
        text = {
            Column {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Phương thức: ${paymentMethod.displayName}")
                Text("Số tiền: ${formatCurrency(totalAmount.toInt())}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "⚠️ PAYMENT TEST MODE",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            if (!isLoading) {
                TextButton(onClick = onDismiss) {
                    Text("Đóng")
                }
            }
        }
    )
}

@Composable
fun PaymentResultDialog(
    status: PaymentStatus,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when (status) {
                    is PaymentStatus.Success -> "Thanh toán thành công!"
                    is PaymentStatus.Failed -> "Thanh toán thất bại"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                when (status) {
                    is PaymentStatus.Success -> {
                        Text("Mã giao dịch: ${status.transactionCode}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("⚠️ PAYMENT TEST MODE - Không phải giao dịch thật")
                    }
                    is PaymentStatus.Failed -> {
                        Text("Vui lòng thử lại sau.")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

fun formatCurrency(amount: Int): String {
    return "${NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount)} đ"
}

