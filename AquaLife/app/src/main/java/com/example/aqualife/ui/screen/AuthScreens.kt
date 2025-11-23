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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

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
fun LoginScreen(navController: NavController) {
    var contact by remember { mutableStateOf("") } // Email hoặc SĐT
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        Text(text = "Thế giới thủy sinh", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00695C))

        Spacer(modifier = Modifier.height(40.dp))
        Text("Đăng nhập", fontWeight = FontWeight.Bold, fontSize = 24.sp)

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = contact, onValueChange = { contact = it },
            label = { Text("Email hoặc Số điện thoại") }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Mật khẩu") }, visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                // Logic kiểm tra đăng nhập chặt chẽ hơn
                if (contact.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                } else if (UserManager.checkLogin(contact, password)) {
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                } else {
                    Toast.makeText(context, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Đăng nhập", fontWeight = FontWeight.Bold)
        }
        // ... (Giữ nguyên phần nút Google/Facebook bên dưới)
        Spacer(modifier = Modifier.height(20.dp))
        Text("Hoặc đăng nhập bằng", color = Color.Gray)
        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { navController.navigate("google_login") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.LightGray)) {
            Text("Google", color = Color.Black)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { navController.navigate("facebook_login") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))) {
            Text("Facebook", color = Color.White)
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row {
            Text("Chưa có tài khoản? ")
            Text(text = "Đăng ký ngay", color = Color(0xFF00695C), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { navController.navigate("register") })
        }
    }
}

// --- 2. MÀN HÌNH ĐĂNG KÝ (CẬP NHẬT LOGIC CHECK OTP) ---
@Composable
fun RegisterScreen(navController: NavController) {
    var contact by remember { mutableStateOf("") } // Thay tên biến thành contact cho rõ nghĩa
    var pass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(40.dp))
        Text("Đăng ký tài khoản", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(30.dp))

        // Chỉ cho nhập Email hoặc SĐT
        OutlinedTextField(
            value = contact, onValueChange = { contact = it },
            label = { Text("Email hoặc Số điện thoại") }, // Yêu cầu rõ ràng
            placeholder = { Text("ví dụ: duy@gmail.com") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Mật khẩu") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = confirmPass, onValueChange = { confirmPass = it }, label = { Text("Nhập lại mật khẩu") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                // --- LOGIC MỚI: KIỂM TRA ĐỊNH DẠNG ---
                if (!isValidEmail(contact) && !isValidPhone(contact)) {
                    // Nếu không phải Email, cũng không phải SĐT -> Báo lỗi
                    Toast.makeText(context, "Vui lòng nhập đúng Email hoặc SĐT để nhận OTP!", Toast.LENGTH_LONG).show()
                } else if (pass.isEmpty() || pass != confirmPass) {
                    Toast.makeText(context, "Mật khẩu không khớp hoặc để trống", Toast.LENGTH_SHORT).show()
                } else {
                    // Thông tin hợp lệ -> Lưu vào DB ảo và chuyển OTP
                    UserManager.register(contact, pass)
                    navController.navigate("otp")
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C))
        ) { Text("Tiếp tục") }

        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = { navController.popBackStack() }) { Text("Quay lại đăng nhập", color = Color.Gray) }
    }
}

// --- 3. MÀN HÌNH OTP (Giữ nguyên) ---
@Composable
fun OTPScreen(navController: NavController) {
    var otpValue by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(60.dp))
        Text("XÁC THỰC OTP", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Mã OTP mặc định là 0000", color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = otpValue, onValueChange = { if (it.length <= 4) otpValue = it },
            label = { Text("Nhập 4 số OTP") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                if (otpValue == "0000") navController.navigate("login") { popUpTo("welcome") }
            },
            modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C))
        ) { Text("Xác thực") }
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