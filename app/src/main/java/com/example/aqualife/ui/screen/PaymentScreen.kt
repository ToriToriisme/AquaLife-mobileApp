package com.example.aqualife.ui.screen

// ============================================================================
// ANDROID IMPORTS
// ============================================================================
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns

// ============================================================================
// ANDROIDX IMPORTS
// ============================================================================
// Activity & Result
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent

// Compose Foundation
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions

// Compose Material Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner

// Compose Material3
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

// Compose Runtime
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable

// Compose UI
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Navigation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

// ============================================================================
// THIRD-PARTY IMPORTS
// ============================================================================
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

// ============================================================================
// LOCAL IMPORTS
// ============================================================================
import com.example.aqualife.ui.viewmodel.CartItemUi
import com.example.aqualife.ui.viewmodel.CartViewModel
import com.example.aqualife.ui.viewmodel.NotificationViewModel
import com.example.aqualife.ui.viewmodel.PaymentMethod
import com.example.aqualife.ui.viewmodel.PaymentUiState
import com.example.aqualife.ui.viewmodel.PaymentViewModel
import com.example.aqualife.utils.FormatUtils
import com.example.aqualife.utils.MomoQrGenerator

// ============================================================================
// MAIN PAYMENT SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    cartViewModel: CartViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    // State Management
    var fullName by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.MOMO) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var readyState by remember { mutableStateOf<PaymentUiState.Ready?>(null) }

    // ViewModel & Context
    val totalPrice by cartViewModel.totalPrice.collectAsState()
    val paymentState by paymentViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    // Handle Payment State Changes
    LaunchedEffect(paymentState) {
        when (val state = paymentState) {
            is PaymentUiState.Ready -> {
                readyState = state
            }
            is PaymentUiState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
                paymentViewModel.resetState()
            }
            is PaymentUiState.Processing -> {
                // Keep processing state
            }
            else -> Unit
        }
    }

    // Form Validation
    fun validateForm(): Boolean {
        var isValid = true
        if (fullName.isBlank()) {
            nameError = "Vui lòng nhập họ tên"
            isValid = false
        } else {
            nameError = null
        }
        if (phoneNumber.isBlank() || !Patterns.PHONE.matcher(phoneNumber).matches()) {
            phoneError = "Số điện thoại không hợp lệ"
            isValid = false
        } else {
            phoneError = null
        }
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Email không hợp lệ"
            isValid = false
        } else {
            emailError = null
        }
        return isValid
    }

    // Handle Payment Link Opening (Fixed for Deep Links)
    fun openPaymentLink(link: String) {
        try {
            val uri = Uri.parse(link)
            
            // Check if it's a deep link (momo://, vnpay://, etc.)
            val isDeepLink = uri.scheme?.let { 
                it == "momo" || it == "vnpay" || !it.startsWith("http")
            } ?: false

            if (isDeepLink) {
                // Use Intent.ACTION_VIEW for deep links
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                
                // Check if there's an app that can handle this intent
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    // Fallback: Show message to install app or copy link
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            "Ứng dụng ${selectedPaymentMethod.displayName} chưa được cài đặt. Vui lòng cài đặt ứng dụng hoặc sao chép liên kết."
                        )
                    }
                }
            } else {
                // Use CustomTabsIntent for HTTP/HTTPS links
                try {
                    CustomTabsIntent.Builder().build().launchUrl(context, uri)
                } catch (e: Exception) {
                    // Fallback to browser
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            }
        } catch (ex: Exception) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Không thể mở liên kết. Vui lòng sao chép và mở thủ công."
                )
            }
        }
    }

    // UI
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thanh toán", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tóm tắt đơn hàng", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tổng cộng")
                        Text(
                            FormatUtils.formatCurrency(totalPrice.toInt()),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Red
                        )
                    }
                }
            }

            // Contact Information Section
            Text("Thông tin liên hệ", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Họ và tên") },
                singleLine = true,
                isError = nameError != null,
                supportingText = {
                    nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Số điện thoại") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError != null,
                supportingText = {
                    phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email nhận hóa đơn") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                supportingText = {
                    emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Payment Method Selection
            Text("Phương thức thanh toán", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            PaymentMethodCard(
                method = PaymentMethod.MOMO,
                isSelected = selectedPaymentMethod == PaymentMethod.MOMO,
                onClick = { selectedPaymentMethod = PaymentMethod.MOMO }
            )

            PaymentMethodCard(
                method = PaymentMethod.VNPAY,
                isSelected = selectedPaymentMethod == PaymentMethod.VNPAY,
                onClick = { selectedPaymentMethod = PaymentMethod.VNPAY }
            )

            PaymentMethodCard(
                method = PaymentMethod.BANK,
                isSelected = selectedPaymentMethod == PaymentMethod.BANK,
                onClick = {
                    selectedPaymentMethod = PaymentMethod.BANK
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Hiện chỉ hỗ trợ MoMo hoặc VNPay. Vui lòng chọn phương thức khác.")
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Payment Button
            Button(
                onClick = {
                    if (validateForm()) {
                        paymentViewModel.initiatePayment(
                            name = fullName,
                            phone = phoneNumber,
                            email = email,
                            amount = totalPrice.toLong(),
                            method = selectedPaymentMethod
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Thanh toán với ${selectedPaymentMethod.displayName}", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Processing Dialog
    if (paymentState is PaymentUiState.Processing) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Đang tạo đơn thanh toán...") },
            text = { Text("Vui lòng giữ kết nối Internet ổn định trong khi xử lý.") },
            confirmButton = { }
        )
    }

    // Payment QR Dialog
    readyState?.let { ready ->
        PaymentQrDialog(
            ready = ready,
            cartItems = cartViewModel.cartItems.value,
            onDismiss = { 
                readyState = null
                paymentViewModel.resetState()
            },
            onConfirmPayment = {
                // Create success notification with fish image
                val cartItems = cartViewModel.cartItems.value
                val firstFish = cartItems.firstOrNull()?.fish
                
                coroutineScope.launch {
                    // Create notification with fish image
                    val notificationId = "order_${System.currentTimeMillis()}"
                    val fishName = firstFish?.name ?: "sản phẩm"
                    val fishImageUrl = firstFish?.imageUrl
                    val totalItems = cartItems.sumOf { it.quantity }
                    val itemText = if (totalItems == 1) {
                        fishName
                    } else {
                        "$totalItems sản phẩm"
                    }
                    
                    notificationViewModel.createOrderNotification(
                        notificationId = notificationId,
                        title = "Đặt hàng thành công!",
                        message = "Bạn đã đặt hàng thành công. Cảm ơn bạn đã mua $itemText tại AquaLife!",
                        fishImageUrl = fishImageUrl
                    )
                    
                    // Clear cart after successful payment
                    cartViewModel.clearCart()
                    
                    // Show success message
                    snackbarHostState.showSnackbar("Bạn đã đặt hàng thành công!")
                    
                    // Close dialog and navigate back
                    readyState = null
                    paymentViewModel.resetState()
                    navController.popBackStack()
                }
            },
            onOpenLink = { link -> openPaymentLink(link) },
            onCopyLink = { link ->
                val clip = ClipData.newPlainText("Payment Link", link)
                clipboard.setPrimaryClip(clip)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Đã sao chép liên kết thanh toán")
                }
            }
        )
    }
}

// ============================================================================
// PAYMENT METHOD CARD COMPONENT
// ============================================================================

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
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = method.displayName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(end = 16.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ============================================================================
// PAYMENT QR DIALOG COMPONENT
// ============================================================================

@Composable
private fun PaymentQrDialog(
    ready: PaymentUiState.Ready,
    cartItems: List<CartItemUi>,
    onDismiss: () -> Unit,
    onConfirmPayment: () -> Unit,
    onOpenLink: (String) -> Unit,
    onCopyLink: (String) -> Unit
) {
    // Countdown Timer
    var remainingMillis by remember(ready.expireAt) {
        mutableLongStateOf(max(ready.expireAt - System.currentTimeMillis(), 0))
    }
    
    LaunchedEffect(ready.expireAt) {
        while (remainingMillis > 0) {
            delay(1000)
            remainingMillis = max(ready.expireAt - System.currentTimeMillis(), 0)
        }
    }
    
    val expired = remainingMillis <= 0
    val minutes = (remainingMillis / 1000) / 60
    val seconds = (remainingMillis / 1000) % 60
    val countdownText = if (expired) "Mã đã hết hạn" else String.format("%02d:%02d phút", minutes, seconds)
    
    // Transfer Information
    val transferInfo = when (ready.method) {
        PaymentMethod.MOMO -> {
            val momoInfo = MomoQrGenerator.getMomoTransferInfo()
            "Số điện thoại: ${momoInfo.phone}\nTên: ${momoInfo.name}"
        }
        PaymentMethod.BANK -> {
            val bankInfo = MomoQrGenerator.getBankTransferInfo()
            "STK: ${bankInfo.accountNumber}\nNgân hàng: ${bankInfo.bankName}\nTên: ${bankInfo.accountName}"
        }
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Column {
                Text("Quét mã ${ready.method.displayName}", fontWeight = FontWeight.Bold)
                ready.orderId?.let { 
                    Text("Mã đơn: $it", fontSize = 12.sp, color = Color.Gray) 
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                ready.qrUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .size(240.dp)
                            .padding(8.dp)
                    )
                } ?: Text("Không tìm thấy mã QR. Vui lòng mở liên kết để thanh toán.", color = Color.Red)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                transferInfo?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Thông tin chuyển khoản:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                it,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Mã hết hạn sau: $countdownText",
                    fontSize = 12.sp,
                    color = if (expired) Color.Red else Color.Gray
                )
            }
        },
        confirmButton = {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                ready.payUrl?.let { link ->
                    Button(
                        onClick = { onOpenLink(link) },
                        enabled = !expired,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mở ${ready.method.displayName}")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { onCopyLink(link) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sao chép liên kết")
                    }
                }
                Button(
                    onClick = onConfirmPayment,
                    enabled = !expired,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Xác nhận đã chuyển khoản", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onDismiss) {
                    Text(if (expired) "Đóng" else "Hủy")
                }
            }
        }
    )
}
