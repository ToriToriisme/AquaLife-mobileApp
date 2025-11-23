package com.example.aqualife.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aqualife.R
import kotlinx.coroutines.delay

// Model dữ liệu
data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val subtitle: String
)

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    // Biến trạng thái: true = Hiện màn hình Logo (Start), false = Hiện màn hình Slide
    var showStartScreen by remember { mutableStateOf(true) }

    if (showStartScreen) {
        // Màn hình 1: Logo + Nút Bắt đầu
        StartScreen(
            onStartClick = { showStartScreen = false } // Bấm nút thì chuyển biến thành false
        )
    } else {
        // Màn hình 2: Slide ảnh tự chạy
        OnboardingContent(
            onLogin = onNavigateToLogin,
            onRegister = onNavigateToRegister
        )
    }
}

// --- MÀN HÌNH 1: LOGO & NÚT BẮT ĐẦU ---
@Composable
fun StartScreen(onStartClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. Hình nền bg_logo
        Image(
            painter = painterResource(id = R.drawable.bg_logo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Lớp phủ tối nhẹ để chữ và logo rõ hơn
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))

        // 2. Logo và Tên App (Ở giữa)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo), // Nhớ dùng file .png đã xóa nền
                contentDescription = "Logo",
                modifier = Modifier.size(160.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Thế giới thủy sinh",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // 3. Nút Bắt đầu (Ở dưới cùng)
        Button(
            onClick = onStartClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9FA8DA)),
            shape = RoundedCornerShape(28.dp) // Bo tròn nút
        ) {
            Text(
                text = "Bắt đầu",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

// --- MÀN HÌNH 2: SLIDE ẢNH TỰ CHẠY ---
@Composable
fun OnboardingContent(onLogin: () -> Unit, onRegister: () -> Unit) {
    val pages = listOf(
        OnboardingPage(R.drawable.bg_fish, "Welcome", "Thế giới thủy sinh"),
        OnboardingPage(R.drawable.bg_dolphin, "Khám phá", "Đại dương bao la"),
        OnboardingPage(R.drawable.bg_jellyfish, "Kết nối", "Cộng đồng yêu cá")
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    // Logic tự động chạy Slide (Auto-scroll)
    LaunchedEffect(pagerState) {
        while (true) {
            delay(3000) // Đợi 3 giây
            val nextPage = (pagerState.currentPage + 1) % pages.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Slide ảnh nền
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            Image(
                painter = painterResource(id = pages[pageIndex].imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Gradient nền dưới chân
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF1A1A2E))
                    )
                )
        )

        // Nội dung chữ và nút Đăng ký/Đăng nhập
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = pages[pagerState.currentPage].title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5C6BC0)
            )
            Text(
                text = pages[pagerState.currentPage].subtitle,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7986CB),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Indicator (Dấu chấm)
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                repeat(pages.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.White else Color.Gray
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }

            // Hai nút Đăng ký và Đăng nhập
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onRegister,
                    modifier = Modifier.weight(1f).height(50.dp).padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9FA8DA))
                ) {
                    Text(text = "Đăng ký", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onLogin,
                    modifier = Modifier.weight(1f).height(50.dp).padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9FA8DA))
                ) {
                    Text(text = "Đăng nhập", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}