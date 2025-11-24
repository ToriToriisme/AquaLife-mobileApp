package com.example.aqualife.ui.screen

import android.util.Patterns
import android.widget.Toast
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
import com.example.aqualife.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await

// --- HÀM KIỂM TRA DỮ LIỆU (VALIDATION) ---
fun isValidEmail(email: String): Boolean {
    return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isValidPhone(phone: String): Boolean {
    return phone.isNotEmpty() && phone.all { it.isDigit() } && phone.length >= 10
}

// --- GIẢ LẬP DATABASE ---
object UserManager {
    // Lưu trữ: Key = Email/Phone, Value = Password
    private val users = mutableMapOf<String, String>()

    fun register(contact: String, pass: String) {
        users[contact] = pass
    }

    fun checkLogin(contact: String, pass: String): Boolean {
        // Kiểm tra xem tài khoản có trong database không VÀ mật khẩu có khớp không
        return users.containsKey(contact) && users[contact] == pass
    }
}

// --- 1. MÀN HÌNH ĐĂNG NHẬP ---
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
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val emailFocusRequester = remember { FocusRequester() }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.authError.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    
    // Auto-navigate when login successful and email verified
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            try {
                // Reload user to get latest verification status
                user.reload().await()
                if (user.isEmailVerified) {
                    navController.navigate("home") { 
                        popUpTo("login") { inclusive = true } 
                    }
                }
            } catch (e: Exception) {
                // If reload fails, check current status
                if (user.isEmailVerified) {
                    navController.navigate("home") { 
                        popUpTo("login") { inclusive = true } 
                    }
                }
            }
        }
    }
    
    // Show error messages
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }
    
    // Request focus after a delay to ensure keyboard shows
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
                        Color(0xFF26A69A)
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
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "Thế giới thủy sinh",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(40.dp))
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
                        "Đăng nhập",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color(0xFF00695C)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = email, 
                        onValueChange = { email = it },
                        label = { Text("Email hoặc tên đăng nhập") }, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(emailFocusRequester)
                            .onFocusChanged { emailFocused = it.isFocused },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF26A69A),
                            unfocusedBorderColor = Color.Gray
                        ),
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = if (emailFocused) Color(0xFF26A69A) else Color.Gray
                            ) 
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password, 
                        onValueChange = { password = it },
                        label = { Text("Mật khẩu") }, 
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF26A69A),
                            unfocusedBorderColor = Color.Gray
                        ),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = Color.Gray
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            if (email.isEmpty() || password.isEmpty()) {
                                Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.login(email, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF26A69A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Đăng nhập", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Hoặc đăng nhập bằng", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { navController.navigate("google_login") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(1.dp, Color.LightGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Google", fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { navController.navigate("facebook_login") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Facebook", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row {
                        Text("Chưa có tài khoản? ", color = Color.Gray)
                        Text(
                            text = "Đăng ký ngay",
                            color = Color(0xFF26A69A),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { navController.navigate("register") }
                        )
                    }
                }
            }
        }
    }
}

// --- 2. MÀN HÌNH ĐĂNG KÝ (CẬP NHẬT LOGIC CHECK OTP) ---
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
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val emailFocusRequester = remember { FocusRequester() }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.authError.collectAsState()
    val registrationSuccess = remember { mutableStateOf(false) }
    
    // Show error messages
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }
    
    // Handle successful registration
    LaunchedEffect(viewModel.registrationSuccess) {
        if (viewModel.registrationSuccess.value) {
            registrationSuccess.value = true
            Toast.makeText(
                context,
                "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.",
                Toast.LENGTH_LONG
            ).show()
            // Navigate to login after a delay
            kotlinx.coroutines.delay(2000)
            navController.navigate("login") {
                popUpTo("register") { inclusive = true }
            }
        }
    }
    
    // Request focus after a delay to ensure keyboard shows
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
                        Color(0xFF26A69A)
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
                        "Đăng ký tài khoản",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00695C)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = email, 
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        placeholder = { Text("ví dụ: duy@gmail.com") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(emailFocusRequester)
                            .onFocusChanged { emailFocused = it.isFocused },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF26A69A),
                            unfocusedBorderColor = Color.Gray
                        ),
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = if (emailFocused) Color(0xFF26A69A) else Color.Gray
                            ) 
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = displayName, 
                        onValueChange = { displayName = it },
                        label = { Text("Tên đăng nhập") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { nameFocused = it.isFocused },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF26A69A),
                            unfocusedBorderColor = Color.Gray
                        ),
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (nameFocused) Color(0xFF26A69A) else Color.Gray
                            ) 
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = pass, 
                        onValueChange = { pass = it }, 
                        label = { Text("Mật khẩu") }, 
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(), 
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF26A69A),
                            unfocusedBorderColor = Color.Gray
                        ),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = confirmPass, 
                        onValueChange = { confirmPass = it }, 
                        label = { Text("Nhập lại mật khẩu") }, 
                        visualTransformation = if (confirmPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(), 
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF26A69A),
                            unfocusedBorderColor = Color.Gray
                        ),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            if (!isValidEmail(email)) {
                                Toast.makeText(context, "Vui lòng nhập đúng định dạng Email!", Toast.LENGTH_SHORT).show()
                            } else if (displayName.isEmpty()) {
                                Toast.makeText(context, "Vui lòng nhập tên đăng nhập", Toast.LENGTH_SHORT).show()
                            } else if (pass.isEmpty() || pass.length < 6) {
                                Toast.makeText(context, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                            } else if (pass != confirmPass) {
                                Toast.makeText(context, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.register(email, pass, displayName)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF26A69A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) { 
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Đăng ký", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { navController.popBackStack() }) { 
                        Text("Quay lại đăng nhập", color = Color.Gray) 
                    }
                }
            }
        }
    }
}

// --- 3. MÀN HÌNH OTP (Cập nhật để tránh crash) ---
@Composable
fun OTPScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var otpValue by remember { mutableStateOf("") }
    val context = LocalContext.current
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
                                CoroutineScope(Dispatchers.Main).launch {
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