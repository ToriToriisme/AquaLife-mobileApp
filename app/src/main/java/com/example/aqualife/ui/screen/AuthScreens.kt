package com.example.aqualife.ui.screen

import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import com.example.aqualife.ui.viewmodel.AuthState
import com.example.aqualife.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.rememberCoroutineScope

// --- HÀM KIỂM TRA DỮ LIỆU (VALIDATION) ---
fun isValidEmail(email: String): Boolean {
    return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isValidPhone(phone: String): Boolean {
    return phone.isNotEmpty() && phone.all { it.isDigit() } && phone.length >= 10
}

// --- 1. MÀN HÌNH ĐĂNG NHẬP (IMPROVED UI/UX) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var canNavigate by remember { mutableStateOf(true) }
    var hasNavigated by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val emailFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.authError.collectAsState()
    val authState by viewModel.authState.collectAsState()
    
    fun navigateSafely(action: () -> Unit) {
        if (!canNavigate) return
        canNavigate = false
        action()
        coroutineScope.launch {
            delay(500)
            canNavigate = true
        }
    }
    
    // Animation states
    val cardScale by animateFloatAsState(
        targetValue = if (isLoading) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )
    
    val cardAlpha by animateFloatAsState(
        targetValue = if (isLoading) 0.9f else 1f,
        label = "cardAlpha"
    )
    
    LaunchedEffect(key1 = authState) {
        android.util.Log.d("LoginScreen", "AuthState changed to: $authState")
        when (val state = authState) {
            AuthState.Success -> {
                if (!hasNavigated && canNavigate) {
                    hasNavigated = true
                    canNavigate = false
                    showVerificationDialog = false
                    android.util.Log.d("LoginScreen", "Navigating to home")
                    delay(150) // Small delay to ensure state is stable
                    try {
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true } // Clear entire back stack
                            launchSingleTop = true
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("LoginScreen", "Navigation error", e)
                        Toast.makeText(context, "Lỗi điều hướng: ${e.message}", Toast.LENGTH_SHORT).show()
                        hasNavigated = false
                        canNavigate = true
                    }
                    viewModel.resetState()
                }
            }
            AuthState.VerificationRequired -> {
                showVerificationDialog = true
            }
            AuthState.Idle -> {
                showVerificationDialog = false
                hasNavigated = false
            }
            is AuthState.Error -> {
                showVerificationDialog = false
                hasNavigated = false
            }
            AuthState.Loading -> {
                // Do nothing while loading
            }
        }
    }
    
    // Show error messages with better UX
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            val isVerificationMessage = errorMessage.contains("xác thực", ignoreCase = true)
            when {
                errorMessage.contains("email", ignoreCase = true) && !isVerificationMessage -> {
                    emailError = errorMessage
                }
                errorMessage.contains("password", ignoreCase = true) ||
                    errorMessage.contains("mật khẩu", ignoreCase = true) -> {
                    passwordError = errorMessage
                }
                else -> {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    // Clear errors when user types
    LaunchedEffect(email) {
        emailError = null
    }
    
    LaunchedEffect(password) {
        passwordError = null
    }
    
    // Request focus after a delay
    LaunchedEffect(Unit) {
        delay(300)
        emailFocusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF80CBC4),
                        Color(0xFF4DB6AC),
                        Color(0xFF26A69A),
                        Color(0xFF00897B)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Animated logo/title
            Text(
                text = "Thế giới thủy sinh",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(cardAlpha)
            )
            
            Text(
                text = "Khám phá đại dương trong tầm tay",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 8.dp).alpha(cardAlpha)
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            // Animated card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(cardScale)
                    .alpha(cardAlpha),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Đăng nhập",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color(0xFF00695C)
                    )

                    Spacer(modifier = Modifier.height(28.dp))
                    
                    // Email field with error handling
                    OutlinedTextField(
                        value = email, 
                        onValueChange = { 
                            email = it
                            emailError = null
                        },
                        label = { Text("Email hoặc tên đăng nhập") },
                        placeholder = { Text("Nhập email của bạn") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(emailFocusRequester)
                            .onFocusChanged { emailFocused = it.isFocused },
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        isError = emailError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF26A69A),
                            unfocusedBorderColor = if (emailError != null) MaterialTheme.colorScheme.error else Color.Gray,
                            focusedLabelColor = Color(0xFF26A69A),
                            unfocusedLabelColor = Color.Gray
                        ),
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = when {
                                    emailError != null -> MaterialTheme.colorScheme.error
                                    emailFocused -> Color(0xFF26A69A)
                                    else -> Color.Gray
                                }
                            ) 
                        }
                    )
                    
                    // Error message for email
                    AnimatedVisibility(
                        visible = emailError != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Text(
                            text = emailError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Password field with error handling
                    OutlinedTextField(
                        value = password, 
                        onValueChange = { 
                            password = it
                            passwordError = null
                        },
                        label = { Text("Mật khẩu") },
                        placeholder = { Text("Nhập mật khẩu") },
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { passwordFocused = it.isFocused },
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        isError = passwordError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF26A69A),
                            unfocusedBorderColor = if (passwordError != null) MaterialTheme.colorScheme.error else Color.Gray,
                            focusedLabelColor = Color(0xFF26A69A),
                            unfocusedLabelColor = Color.Gray
                        ),
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = when {
                                    passwordError != null -> MaterialTheme.colorScheme.error
                                    passwordFocused -> Color(0xFF26A69A)
                                    else -> Color.Gray
                                }
                            ) 
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                                    tint = if (passwordError != null) MaterialTheme.colorScheme.error else Color.Gray
                                )
                            }
                        }
                    )
                    
                    // Error message for password
                    AnimatedVisibility(
                        visible = passwordError != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Text(
                            text = passwordError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    
                    // Login button with better feedback
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            val trimmedEmail = email.trim()
                            val trimmedPassword = password.trim()
                            val isAdminCredentials = trimmedEmail == "admin123" && trimmedPassword == "admin123"

                            var hasError = false

                            if (!isAdminCredentials) {
                                if (trimmedEmail.isEmpty()) {
                                    emailError = "Vui lòng nhập email"
                                    hasError = true
                                } else if (!isValidEmail(trimmedEmail)) {
                                    emailError = "Email không hợp lệ"
                                    hasError = true
                                }

                                if (trimmedPassword.isEmpty()) {
                                    passwordError = "Vui lòng nhập mật khẩu"
                                    hasError = true
                                } else if (trimmedPassword.length < 6) {
                                    passwordError = "Mật khẩu phải có ít nhất 6 ký tự"
                                    hasError = true
                                }
                            } else {
                                emailError = null
                                passwordError = null
                            }

                            if (!hasError || isAdminCredentials) {
                                viewModel.login(trimmedEmail, trimmedPassword)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF26A69A),
                            disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                "Đăng nhập",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Divider with text
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            "Hoặc",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Social login buttons
                    OutlinedButton(
                        onClick = { navController.navigate("google_login") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(1.5.dp, Color.LightGray),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đăng nhập với Google", fontWeight = FontWeight.Medium)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { navController.navigate("facebook_login") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "Đăng nhập với Facebook",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Register link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Chưa có tài khoản? ", color = Color.Gray, fontSize = 14.sp)
                        Text(
                            text = "Đăng ký ngay",
                            color = Color(0xFF26A69A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { 
                                navigateSafely { navController.navigate("register") }
                            }
                        )
                    }
                }
            }
        }
        
        if (showVerificationDialog) {
            EmailVerificationDialog(
                email = email,
                isChecking = isLoading,
                onDismiss = {
                    showVerificationDialog = false
                    viewModel.resetState()
                },
                onCheckStatus = { viewModel.checkEmailStatus() },
                onResend = { viewModel.resendVerificationEmail() }
            )
        }
    }
}

// --- 2. MÀN HÌNH ĐĂNG KÝ (IMPROVED UI/UX) ---
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }
    var nameFocused by remember { mutableStateOf(false) }
    var passFocused by remember { mutableStateOf(false) }
    var confirmPassFocused by remember { mutableStateOf(false) }
    
    var emailError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var passError by remember { mutableStateOf<String?>(null) }
    var confirmPassError by remember { mutableStateOf<String?>(null) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var canNavigate by remember { mutableStateOf(true) }
    var hasNavigated by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val emailFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.authError.collectAsState()
    val authState by viewModel.authState.collectAsState()

    fun navigateSafely(action: () -> Unit) {
        if (!canNavigate) return
        canNavigate = false
        action()
        coroutineScope.launch {
            delay(500)
            canNavigate = true
        }
    }
    
    // Animation states
    val cardScale by animateFloatAsState(
        targetValue = if (isLoading) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )
    
    val cardAlpha by animateFloatAsState(
        targetValue = if (isLoading) 0.9f else 1f,
        label = "cardAlpha"
    )
    
    // Show error messages
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            val isVerificationMessage = errorMessage.contains("xác thực", ignoreCase = true)
            when {
                errorMessage.contains("email", ignoreCase = true) && !isVerificationMessage -> {
                    emailError = errorMessage
                }
                errorMessage.contains("password", ignoreCase = true) || 
                errorMessage.contains("mật khẩu", ignoreCase = true) -> {
                    passError = errorMessage
                }
                else -> {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            AuthState.VerificationRequired -> {
                showVerificationDialog = true
            }
            AuthState.Success -> {
                if (!hasNavigated) {
                    hasNavigated = true
                    showVerificationDialog = false
                    Toast.makeText(
                        context,
                        "Tài khoản đã được xác thực! Vui lòng đăng nhập.",
                        Toast.LENGTH_LONG
                    ).show()
                    delay(100) // Small delay to ensure state is stable
                    try {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("RegisterScreen", "Navigation error", e)
                        hasNavigated = false
                    }
                    viewModel.resetState()
                }
            }
            AuthState.Idle -> {
                showVerificationDialog = false
                hasNavigated = false
            }
            is AuthState.Error -> {
                hasNavigated = false
            }
            else -> Unit
        }
    }
    
    // Request focus after a delay
    LaunchedEffect(Unit) {
        delay(300)
        emailFocusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF80CBC4),
                        Color(0xFF4DB6AC),
                        Color(0xFF26A69A),
                        Color(0xFF00897B)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(cardScale)
                    .alpha(cardAlpha),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Đăng ký tài khoản",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00695C)
                    )
                    
                    Text(
                        "Tạo tài khoản mới để bắt đầu",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(28.dp))

                    // Email field
                    OutlinedTextField(
                        value = email, 
                        onValueChange = { 
                            email = it
                            emailError = null
                        },
                        label = { Text("Email") },
                        placeholder = { Text("ví dụ: duy@gmail.com") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(emailFocusRequester)
                            .onFocusChanged { emailFocused = it.isFocused },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        isError = emailError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF26A69A),
                            unfocusedBorderColor = if (emailError != null) MaterialTheme.colorScheme.error else Color.Gray,
                            focusedLabelColor = Color(0xFF26A69A),
                            unfocusedLabelColor = Color.Gray
                        ),
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = when {
                                    emailError != null -> MaterialTheme.colorScheme.error
                                    emailFocused -> Color(0xFF26A69A)
                                    else -> Color.Gray
                                }
                            ) 
                        }
                    )
                    
                    AnimatedVisibility(
                        visible = emailError != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Text(
                            text = emailError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Display name field
                    OutlinedTextField(
                        value = displayName, 
                        onValueChange = { 
                            displayName = it
                            nameError = null
                        },
                        label = { Text("Tên đăng nhập") },
                        placeholder = { Text("Nhập tên của bạn") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { nameFocused = it.isFocused },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        isError = nameError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF26A69A),
                            unfocusedBorderColor = if (nameError != null) MaterialTheme.colorScheme.error else Color.Gray,
                            focusedLabelColor = Color(0xFF26A69A),
                            unfocusedLabelColor = Color.Gray
                        ),
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = when {
                                    nameError != null -> MaterialTheme.colorScheme.error
                                    nameFocused -> Color(0xFF26A69A)
                                    else -> Color.Gray
                                }
                            ) 
                        }
                    )
                    
                    AnimatedVisibility(
                        visible = nameError != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Text(
                            text = nameError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Password field
                    OutlinedTextField(
                        value = pass, 
                        onValueChange = { 
                            pass = it
                            passError = null
                            if (confirmPass.isNotEmpty() && pass != confirmPass) {
                                confirmPassError = "Mật khẩu không khớp"
                            } else {
                                confirmPassError = null
                            }
                        }, 
                        label = { Text("Mật khẩu") },
                        placeholder = { Text("Tối thiểu 6 ký tự") },
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(), 
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { passFocused = it.isFocused },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        isError = passError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF26A69A),
                            unfocusedBorderColor = if (passError != null) MaterialTheme.colorScheme.error else Color.Gray,
                            focusedLabelColor = Color(0xFF26A69A),
                            unfocusedLabelColor = Color.Gray
                        ),
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = when {
                                    passError != null -> MaterialTheme.colorScheme.error
                                    passFocused -> Color(0xFF26A69A)
                                    else -> Color.Gray
                                }
                            ) 
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                                    tint = if (passError != null) MaterialTheme.colorScheme.error else Color.Gray
                                )
                            }
                        }
                    )
                    
                    AnimatedVisibility(
                        visible = passError != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Text(
                            text = passError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }
                    
                    // Password strength indicator
                    if (pass.isNotEmpty() && passError == null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val strength = when {
                                pass.length < 6 -> 1
                                pass.length < 10 -> 2
                                else -> 3
                            }
                            repeat(3) { index ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .background(
                                            color = if (index < strength) {
                                                when (strength) {
                                                    1 -> Color(0xFFFF5252)
                                                    2 -> Color(0xFFFFC107)
                                                    else -> Color(0xFF4CAF50)
                                                }
                                            } else Color.LightGray,
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Confirm password field
                    OutlinedTextField(
                        value = confirmPass, 
                        onValueChange = { 
                            confirmPass = it
                            confirmPassError = if (it != pass && it.isNotEmpty()) "Mật khẩu không khớp" else null
                        }, 
                        label = { Text("Nhập lại mật khẩu") },
                        placeholder = { Text("Xác nhận mật khẩu") },
                        visualTransformation = if (confirmPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(), 
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { confirmPassFocused = it.isFocused },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        isError = confirmPassError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF26A69A),
                            unfocusedBorderColor = if (confirmPassError != null) MaterialTheme.colorScheme.error else Color.Gray,
                            focusedLabelColor = Color(0xFF26A69A),
                            unfocusedLabelColor = Color.Gray
                        ),
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = when {
                                    confirmPassError != null -> MaterialTheme.colorScheme.error
                                    confirmPassFocused -> Color(0xFF26A69A)
                                    else -> Color.Gray
                                }
                            ) 
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                                    tint = if (confirmPassError != null) MaterialTheme.colorScheme.error else Color.Gray
                                )
                            }
                        }
                    )
                    
                    AnimatedVisibility(
                        visible = confirmPassError != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Text(
                            text = confirmPassError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    
                    // Register button
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            var hasError = false
                            
                            if (!isValidEmail(email)) {
                                emailError = "Vui lòng nhập đúng định dạng Email"
                                hasError = true
                            }
                            
                            if (displayName.isEmpty()) {
                                nameError = "Vui lòng nhập tên đăng nhập"
                                hasError = true
                            } else if (displayName.length < 3) {
                                nameError = "Tên đăng nhập phải có ít nhất 3 ký tự"
                                hasError = true
                            }
                            
                            if (pass.isEmpty()) {
                                passError = "Vui lòng nhập mật khẩu"
                                hasError = true
                            } else if (pass.length < 6) {
                                passError = "Mật khẩu phải có ít nhất 6 ký tự"
                                hasError = true
                            }
                            
                            if (confirmPass.isEmpty()) {
                                confirmPassError = "Vui lòng xác nhận mật khẩu"
                                hasError = true
                            } else if (pass != confirmPass) {
                                confirmPassError = "Mật khẩu không khớp"
                                hasError = true
                            }
                            
                            if (!hasError) {
                                viewModel.register(email, pass, displayName)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF26A69A),
                            disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading && email.isNotEmpty() && displayName.isNotEmpty() && 
                                 pass.isNotEmpty() && confirmPass.isNotEmpty()
                    ) { 
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text("Đăng ký", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    
                    TextButton(
                        onClick = { navigateSafely { navController.popBackStack() } },
                        modifier = Modifier.fillMaxWidth()
                    ) { 
                        Text("Đã có tài khoản? Đăng nhập", color = Color(0xFF26A69A), fontWeight = FontWeight.Medium) 
                    }
                }
            }
        }
        
        if (showVerificationDialog) {
            EmailVerificationDialog(
                email = email,
                isChecking = isLoading,
                onDismiss = {
                    showVerificationDialog = false
                    viewModel.resetState()
                },
                onCheckStatus = { viewModel.checkEmailStatus() },
                onResend = { viewModel.resendVerificationEmail() }
            )
        }
    }
}

@Composable
private fun EmailVerificationDialog(
    email: String,
    isChecking: Boolean,
    onDismiss: () -> Unit,
    onCheckStatus: () -> Unit,
    onResend: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Xác thực email", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                val targetEmail = if (email.isBlank()) "hộp thư của bạn" else email
                Text(
                    text = "Chúng tôi đã gửi liên kết xác nhận tới $targetEmail.\n" +
                        "Sau khi mở liên kết, hãy quay lại ứng dụng và bấm \"Tôi đã xác thực xong\".",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onCheckStatus,
                enabled = !isChecking
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Tôi đã xác thực xong")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onResend,
                enabled = !isChecking
            ) {
                Text("Gửi lại email")
            }
        }
    )
}

// --- 3. MÀN HÌNH OTP (Cập nhật để tránh crash) ---
@Composable
fun OTPScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var otpValue by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    
    // Check if user is verified
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            try {
                user.reload().await()
                if (user.isEmailVerified) {
                    Toast.makeText(context, "Email đã được xác thực!", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") {
                        popUpTo("otp") { inclusive = true }
                    }
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF80CBC4),
                        Color(0xFF4DB6AC),
                        Color(0xFF26A69A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "XÁC THỰC EMAIL",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00695C)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Vui lòng kiểm tra email và nhấp vào link xác thực",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = otpValue,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) otpValue = it },
                        label = { Text("Mã OTP (nếu có)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF26A69A),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            // Resend verification email
                            viewModel.resendVerificationEmail()
                            Toast.makeText(context, "Email xác thực đã được gửi lại!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26A69A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Gửi lại email xác thực", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            // Check verification status
                            currentUser?.let { user ->
                                scope.launch {
                                    try {
                                        user.reload().await()
                                        if (user.isEmailVerified) {
                                            navController.navigate("home") {
                                                popUpTo("otp") { inclusive = true }
                                            }
                                        } else {
                                            Toast.makeText(context, "Email chưa được xác thực. Vui lòng kiểm tra email.", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } ?: run {
                                navController.navigate("login") {
                                    popUpTo("otp") { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26A69A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Kiểm tra xác thực", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TextButton(
                        onClick = { navController.navigate("login") { popUpTo("otp") { inclusive = true } } }
                    ) {
                        Text("Quay lại đăng nhập", color = Color.Gray)
                    }
                }
            }
        }
    }
}

// --- 4. MÀN HÌNH GIẢ LẬP FACEBOOK LOGIN (CẬP NHẬT CHECK ĐỊNH DẠNG) ---
@Composable
fun FacebookLoginScreen(navController: NavController) {
    var fbEmail by remember { mutableStateOf("") }
    var fbPass by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF3b5998)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("facebook", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(30.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Đăng nhập Facebook", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = fbEmail, onValueChange = { fbEmail = it },
                    label = { Text("Số di động hoặc email") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fbPass, onValueChange = { fbPass = it },
                    label = { Text("Mật khẩu Facebook") }, visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // --- LOGIC CHECK FACEBOOK GIẢ LẬP ---
                        // Bắt buộc phải nhập đúng định dạng Email hoặc SĐT và mật khẩu > 5 ký tự
                        if ((isValidEmail(fbEmail) || isValidPhone(fbEmail)) && fbPass.length > 5) {
                            Toast.makeText(context, "Đăng nhập Facebook thành công!", Toast.LENGTH_SHORT).show()
                            navController.navigate("home") { popUpTo("login") { inclusive = true } }
                        } else {
                            Toast.makeText(context, "Email/SĐT hoặc mật khẩu không đúng định dạng!", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
                ) { Text("Đăng nhập") }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("Quay lại", color = Color.White, modifier = Modifier.clickable { navController.popBackStack() })
    }
}

// --- 5. MÀN HÌNH GOOGLE LOGIN (GIỮ NGUYÊN) ---
// (Vì cơ chế Google trên máy thật là chọn từ list có sẵn, không nhập pass thủ công)
@Composable
fun GoogleLoginScreen(navController: NavController) {
    val accounts = listOf("daovanduy2005@gmail.com", "student.transport@hcm.edu.vn", "duy.dev@android.com")

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Đăng nhập bằng Google", fontSize = 20.sp, fontWeight = FontWeight.Medium, modifier = Modifier.align(Alignment.Center))
        }
        Divider()
        Text("Chọn tài khoản để tiếp tục tới AquaLife", modifier = Modifier.padding(16.dp), color = Color.Gray)

        accounts.forEach { account ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "Đào Văn Duy", fontWeight = FontWeight.Bold)
                    Text(text = account, color = Color.Gray, fontSize = 14.sp)
                }
            }
            Divider(color = Color.LightGray, thickness = 0.5.dp)
        }
    }
}

// --- 6. HOME SCREEN (GIỮ NGUYÊN) ---
@Composable
fun HomeScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE0F7FA)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Chào mừng bạn!", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00695C))
            Text("Bạn đã đăng nhập thành công.", fontSize = 16.sp, color = Color(0xFF004D40))
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                navController.navigate("welcome") { popUpTo("home") { inclusive = true } }
            }) { Text("Đăng xuất") }
        }
    }
}