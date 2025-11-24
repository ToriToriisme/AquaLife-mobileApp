package com.example.aqualife.ui.screen

import android.net.Uri
import android.util.Patterns
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.aqualife.ui.viewmodel.CartViewModel
import com.example.aqualife.ui.viewmodel.PaymentMethod
import com.example.aqualife.ui.viewmodel.PaymentUiState
import com.example.aqualife.ui.viewmodel.PaymentViewModel
import com.example.aqualife.utils.FormatUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    cartViewModel: CartViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.MOMO) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var readyState by remember { mutableStateOf<PaymentUiState.Ready?>(null) }

    val totalPrice by cartViewModel.totalPrice.collectAsState()
    val paymentState by paymentViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(paymentState) {
        when (val state = paymentState) {
            is PaymentUiState.Ready -> {
                readyState = state
                paymentViewModel.resetState()
            }
            is PaymentUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                paymentViewModel.resetState()
            }
            else -> Unit
        }
    }

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

    if (paymentState is PaymentUiState.Processing) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Đang tạo đơn MoMo...") },
            text = { Text("Vui lòng giữ kết nối Internet ổn định trong khi xử lý.") },
            confirmButton = { }
        )
    }

    readyState?.let { ready ->
        PaymentQrDialog(
            ready = ready,
            onDismiss = { readyState = null },
            onOpenLink = { link ->
                try {
                    CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(link))
                } catch (ex: Exception) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Không thể mở liên kết: ${ex.localizedMessage ?: "Lỗi không xác định"}")
                    }
                }
            },
            onCopyLink = { link ->
                clipboardManager.setText(AnnotatedString(link))
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Đã sao chép liên kết thanh toán")
                }
            }
        )
    }
}

enum class PaymentMethod(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    MOMO("MoMo", Icons.Default.AccountBalanceWallet),
    VNPAY("VNPay QR", Icons.Default.QrCodeScanner),
    BANK("Ngân hàng", Icons.Default.AccountBalance)
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

@Composable
private fun PaymentQrDialog(
    ready: PaymentUiState.Ready,
    onDismiss: () -> Unit,
    onOpenLink: (String) -> Unit,
    onCopyLink: (String) -> Unit
) {
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quét mã ${ready.method.displayName}") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ready.qrUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                } ?: Text("Không tìm thấy mã QR. Vui lòng mở liên kết để thanh toán.", color = Color.Red)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Mã hết hạn sau: $countdownText")
            }
        },
        confirmButton = {
            Column(horizontalAlignment = Alignment.End) {
                ready.payUrl?.let { link ->
                    Button(
                        onClick = { onOpenLink(link) },
                        enabled = !expired,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mở cổng thanh toán")
                    }
                    TextButton(onClick = { onCopyLink(link) }) {
                        Text("Sao chép liên kết")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(if (expired) "Đóng" else "Đã thanh toán")
                }
            }
        }
    )
}

