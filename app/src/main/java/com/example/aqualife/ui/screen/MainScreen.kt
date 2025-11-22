package com.example.aqualife.ui.screen

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.aqualife.R
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

// --- 1. MODEL DỮ LIỆU ---
data class FishProduct(
    val id: Int, val name: String, val price: String, val priceInt: Int, val imageUrl: String,
    val category: String, val habitat: String, val maxWeight: String, val diet: String
)

data class BannerItem(
    val title: String,
    val imageUrl: String,
    val categoryKey: String // Key dùng để lọc danh sách
)

data class Comment(val userName: String, val content: String)

data class UserPost(
    val id: Int, val imageUrl: String,
    var isLiked: MutableState<Boolean> = mutableStateOf(false),
    val comments: SnapshotStateList<Comment> = mutableStateListOf()
)

data class CartItem(val fish: FishProduct, var quantity: MutableState<Int> = mutableStateOf(1))

// --- 2. DỮ LIỆU ---
fun generateRealFishList(): List<FishProduct> {
    return listOf(
        FishProduct(1, "Cá Hề Nemo", "120.000 đ", 120000, "https://images.unsplash.com/photo-1544552866-d3ed42536cfd?w=500", "Cá biển", "Rạn san hô", "250g", "Tảo, sinh vật phù du"),
        FishProduct(2, "Cá Đuôi Gai Xanh", "350.000 đ", 350000, "https://images.unsplash.com/photo-1520253276376-6230bb9382cb?w=500", "Cá biển", "Rạn san hô", "400g", "Tảo biển"),
        FishProduct(3, "Cá Betta Halfmoon", "80.000 đ", 80000, "https://images.unsplash.com/photo-1534531173927-aeb928d54385?w=500", "Cá cảnh", "Nước ngọt tĩnh", "50g", "Trùng chỉ"),
        FishProduct(4, "Cá Rồng Huyết Long", "15.000.000 đ", 15000000, "https://images.unsplash.com/photo-1621809609483-240d677cb945?w=500", "Cá cảnh", "Sông Amazon", "2kg", "Cá nhỏ, côn trùng"),
        FishProduct(5, "Cá Koi Kohaku", "2.500.000 đ", 2500000, "https://images.unsplash.com/photo-1517363898874-737b6217096a?w=500", "Cá cảnh", "Hồ cá Koi", "5kg", "Cám cá Koi, rau"),
        FishProduct(6, "Cá Dĩa Discus", "400.000 đ", 400000, "https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=500", "Cá cảnh", "Sông Amazon", "300g", "Tim bò xay"),
        FishProduct(7, "Cá La Hán", "1.200.000 đ", 1200000, "https://plus.unsplash.com/premium_photo-1666299356479-42b0a7d079c1?w=500", "Cá cảnh", "Nước ngọt", "1kg", "Tôm tép"),
        FishProduct(8, "Cá Bảy Màu (Guppy)", "10.000 đ", 10000, "https://plus.unsplash.com/premium_photo-1673446573020-164728b02393?w=500", "Cá cảnh", "Nước ngọt", "10g", "Rong rêu"),
        FishProduct(9, "Cá Lóc Hoàng Đế", "850.000 đ", 850000, "https://images.unsplash.com/photo-1597954765323-494dc940d93c?w=500", "Cá sông", "Sông Ấn Độ", "3kg", "Cá mồi"),
        FishProduct(10, "Cá Hồng Két", "150.000 đ", 150000, "https://images.unsplash.com/photo-1518827337089-379e083742c0?w=500", "Cá cảnh", "Lai tạo", "1kg", "Thức ăn viên"),
        FishProduct(11, "Cá Chép Sư Tử", "50.000 đ", 50000, "https://images.unsplash.com/photo-1522853100303-3dc7399491bc?w=500", "Cá sông", "Sông hồ", "2kg", "Tạp ăn"),
        FishProduct(12, "Cá Ali Thái", "45.000 đ", 45000, "https://images.unsplash.com/photo-1522069169874-c58ec4b76be5?w=500", "Cá cảnh", "Hồ Malawi", "200g", "Thức ăn viên"),
        FishProduct(13, "Cá Hải Tượng", "50.000.000 đ", 50000000, "https://images.unsplash.com/photo-1559252820-963df22c9df0?w=500", "Cá sông", "Sông Amazon", "200kg", "Cá lớn, gà vịt"),
        FishProduct(14, "Sứa Mặt Trăng", "150.000 đ", 150000, "https://images.unsplash.com/photo-1500339310309-a34999977824?w=500", "Cá biển", "Đại dương", "1kg", "Ấu trùng Artemia"),
        FishProduct(15, "Cá Heo Mũi Chai", "500.000.000 đ", 500000000, "https://images.unsplash.com/photo-1570481662006-a3a1374699e8?w=500", "Cá biển", "Đại dương", "300kg", "Cá nhỏ, mực"),
        FishProduct(16, "Cá Ngừ Đại Dương", "200.000 đ", 200000, "https://images.unsplash.com/photo-1505252585461-04db1eb84625?w=500", "Cá biển", "Biển khơi", "60kg", "Cá cơm, mực"),
        FishProduct(17, "Cá Thu Nhật", "180.000 đ", 180000, "https://images.unsplash.com/photo-1599084993091-1cb5c0721cc6?w=500", "Cá biển", "Biển ôn đới", "5kg", "Cá nhỏ"),
        FishProduct(18, "Cá Chẽm (Vược)", "220.000 đ", 220000, "https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=500", "Cá nước lợ", "Cửa sông", "10kg", "Tôm, cá nhỏ"),
        FishProduct(19, "Cá Mú Trân Châu", "350.000 đ", 350000, "https://images.unsplash.com/photo-1524704654690-b56c05c78a00?w=500", "Cá biển", "Rạn san hô", "15kg", "Cua, cá con"),
        FishProduct(20, "Cá Bống Tượng", "450.000 đ", 450000, "https://images.unsplash.com/photo-1621809609483-240d677cb945?w=500", "Cá sông", "Sông Cửu Long", "2kg", "Tép, cá con"),
        FishProduct(21, "Cá Trắm Đen", "120.000 đ", 120000, "https://images.unsplash.com/photo-1522853100303-3dc7399491bc?w=500", "Cá sông", "Ao hồ lớn", "30kg", "Ốc, hến"),
        FishProduct(22, "Cá Chim Vây Vàng", "160.000 đ", 160000, "https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=500", "Cá nước lợ", "Ven biển", "3kg", "Tảo, động vật thân mềm"),
        FishProduct(23, "Cá Tai Tượng", "90.000 đ", 90000, "https://images.unsplash.com/photo-1522519965392-1eead138b020?w=500", "Cá cảnh", "Sông Congo", "300g", "Côn trùng, quả"),
        FishProduct(24, "Cá Neon Vua", "15.000 đ", 15000, "https://images.unsplash.com/photo-1522069169874-c58ec4b76be5?w=500", "Cá cảnh", "Sông Nam Mỹ", "5g", "Bo bo, cám mịn"),
        FishProduct(25, "Cá Phượng Hoàng", "40.000 đ", 40000, "https://images.unsplash.com/photo-1544552866-d3ed42536cfd?w=500", "Cá cảnh", "Venezuela", "20g", "Trùng huyết"),
        FishProduct(26, "Cá Ông Tiên", "30.000 đ", 30000, "https://images.unsplash.com/photo-1520253276376-6230bb9382cb?w=500", "Cá cảnh", "Sông Amazon", "100g", "Tép, cám"),
        FishProduct(27, "Cá Sấu Hỏa Tiễn", "300.000 đ", 300000, "https://images.unsplash.com/photo-1559252820-963df22c9df0?w=500", "Cá cảnh", "Bắc Mỹ", "10kg", "Cá mồi"),
        FishProduct(28, "Cá Hô", "1.500.000 đ", 1500000, "https://images.unsplash.com/photo-1599084993091-1cb5c0721cc6?w=500", "Cá sông", "Sông Mê Kông", "100kg", "Rong, trái cây"),
        FishProduct(29, "Cá Chạch Lấu", "400.000 đ", 400000, "https://images.unsplash.com/photo-1597954765323-494dc940d93c?w=500", "Cá sông", "Suối đá", "1kg", "Côn trùng nước"),
        FishProduct(30, "Cá Nâu", "180.000 đ", 180000, "https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=500", "Cá nước lợ", "Rừng ngập mặn", "500g", "Rong rêu")
    )
}

// --- 3. GLOBAL STATE ---
val largeFishList = generateRealFishList()
val favoriteFishIds = mutableStateListOf<Int>()
val globalMyPosts = mutableStateListOf<UserPost>()
var globalProfileName by mutableStateOf("tom_cuon_lap_xuong")
var globalProfileBio by mutableStateOf("Bắt đầu hành trình 10 năm.")
val globalCartItems = mutableStateListOf<CartItem>()

// DỮ LIỆU BANNER (4 LOẠI CÁ) - Ảnh thật chất lượng cao
val banners = listOf(
    BannerItem("Cá Biển", "https://images.unsplash.com/photo-1582967788606-a171f1080ca8?w=800", "Cá biển"),
    BannerItem("Cá Sông", "https://images.unsplash.com/photo-1516967931949-2620222137be?w=800", "Cá sông"),
    BannerItem("Cá Nước Lợ", "https://images.unsplash.com/photo-1516683669143-3b3667902497?w=800", "Cá nước lợ"),
    BannerItem("Cá Cảnh", "https://images.unsplash.com/photo-1522069169874-c58ec4b76be5?w=800", "Cá cảnh")
)

fun formatCurrency(amount: Int) = "${NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount)} đ"
fun parseCurrency(priceStr: String) = priceStr.replace(".", "").replace(" đ", "").replace(",", "").toIntOrNull() ?: 0
fun toggleFavorite(fishId: Int) { if (favoriteFishIds.contains(fishId)) favoriteFishIds.remove(fishId) else favoriteFishIds.add(fishId) }
fun addToCart(fish: FishProduct) { val existing = globalCartItems.find { it.fish.id == fish.id }; if (existing != null) existing.quantity.value++ else globalCartItems.add(CartItem(fish, mutableStateOf(1))) }

// --- 4. THEME MANAGER ---
@Composable
fun AquaLifeThemeWrapper(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    val isDarkTheme = remember { mutableStateOf(prefs.getBoolean("is_dark_mode", false)) }
    val colors = if (isDarkTheme.value) darkColorScheme(primary = Color(0xFF80CBC4), onPrimary = Color.Black, background = Color(0xFF121212), surface = Color(0xFF1E1E1E), onBackground = Color.White, onSurface = Color.White) else lightColorScheme(primary = Color(0xFF00695C), onPrimary = Color.White, background = Color(0xFFF9F9F9), surface = Color.White, onBackground = Color.Black, onSurface = Color.Black)
    MaterialTheme(colorScheme = colors) { CompositionLocalProvider(LocalThemeState provides isDarkTheme) { content() } }
}
val LocalThemeState = compositionLocalOf<MutableState<Boolean>> { error("No theme state provided") }

// --- 5. MAIN SCREEN (CONTAINER) ---
@Composable
fun MainScreen(navController: NavController) {
    AquaLifeThemeWrapper {
        var selectedItem by rememberSaveable { mutableIntStateOf(0) }
        val items = listOf("Home", "Khám phá", "Giỏ hàng", "Thông báo", "Tôi")
        val icons = listOf(Icons.Default.Home, Icons.Default.Search, Icons.Outlined.ShoppingCart, Icons.Outlined.Notifications, Icons.Default.Person)
        Scaffold(bottomBar = { NavigationBar(containerColor = MaterialTheme.colorScheme.surface) { items.forEachIndexed { index, item -> NavigationBarItem(icon = { if (index == 2 && globalCartItems.isNotEmpty()) BadgedBox(badge = { Badge { Text("${globalCartItems.size}") } }) { Icon(icons[index], contentDescription = item) } else Icon(icons[index], contentDescription = item) }, selected = selectedItem == index, onClick = { selectedItem = index }, colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, unselectedIconColor = Color.Gray, indicatorColor = Color.Transparent)) } } }) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).background(MaterialTheme.colorScheme.background)) {
                when (selectedItem) {
                    0 -> HomeScreenContent(navController, onGoToCart = { selectedItem = 2 })
                    1 -> SearchScreen(navController)
                    2 -> CartScreen(navController, onGoToSearch = { selectedItem = 1 }, onBackToSearch = { selectedItem = 1 })
                    4 -> ProfileScreen(navController)
                    else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Chức năng đang phát triển", color = MaterialTheme.colorScheme.onBackground) }
                }
            }
        }
    }
}

// --- 6. HOME SCREEN (BANNER TỰ TRƯỢT & LỌC CÁ) ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(navController: NavController, onGoToCart: () -> Unit, modifier: Modifier = Modifier) {
    val lazyRowState = rememberLazyListState()
    val textColor = MaterialTheme.colorScheme.onBackground
    val bgColor = MaterialTheme.colorScheme.background
    var searchText by rememberSaveable { mutableStateOf("") }
    val searchResults by remember { derivedStateOf { if (searchText.isBlank()) emptyList() else largeFishList.filter { it.name.contains(searchText, ignoreCase = true) } } }

    // State cho Banner tự trượt
    val pagerState = rememberPagerState(pageCount = { banners.size })
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // 3 giây chuyển banner 1 lần
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % banners.size)
        }
    }

    Column(modifier = modifier.fillMaxSize().background(bgColor).padding(16.dp)) {
        OutlinedTextField(value = searchText, onValueChange = { searchText = it }, placeholder = { Text("Tìm kiếm...", color = Color.Gray) }, leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) }, trailingIcon = { if (searchText.isNotEmpty()) IconButton(onClick = { searchText = "" }) { Icon(Icons.Default.Clear, contentDescription = null) } }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, disabledContainerColor = MaterialTheme.colorScheme.surface, unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent))
        Spacer(modifier = Modifier.height(16.dp))
        if (searchText.isNotEmpty()) {
            if (searchResults.isEmpty()) Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Không tìm thấy loại cá nào.", color = Color.Gray) } else { Text("Kết quả tìm kiếm:", fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.padding(bottom = 8.dp)); LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) { items(searchResults) { fish -> FishListItem(fish = fish, navController = navController) } } }
        } else {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                val menuTabs = listOf("♥ Yêu Thích", "🕒 Lịch sử", "👤 Theo dõi", "📦 Orders")
                LazyRow(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) { items(menuTabs) { tab -> Text(text = tab, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if(isSystemInDarkTheme()) Color.LightGray else Color.DarkGray, modifier = Modifier.padding(end = 16.dp).clickable { if (tab.contains("Yêu Thích")) navController.navigate("favorites") else if (tab.contains("Orders")) onGoToCart() }) } }
                Spacer(modifier = Modifier.height(16.dp))

                // --- BANNER TỰ ĐỘNG (THAY CHO CARD TĨNH) ---
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                ) { page ->
                    val banner = banners[page]
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                // Bấm vào banner -> Chuyển sang trang danh sách và lọc theo loại
                                navController.navigate("fish_list/${banner.categoryKey}")
                            }
                    ) {
                        Box {
                            AsyncImage(
                                model = banner.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Lớp phủ đen mờ để chữ dễ đọc
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                                Text(banner.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Loại Cá", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor); Spacer(modifier = Modifier.height(12.dp))
                val categories = listOf("Cá biển" to R.drawable.img_ca_nuoc_bien, "Cá sông" to R.drawable.img_ca_song, "Cá nước lợ" to R.drawable.img_ca_nuoc_lo, "Cá cảnh" to R.drawable.img_ca_kieng)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { categories.forEach { (name, img) -> Column(horizontalAlignment = Alignment.CenterHorizontally) { Image(painter = painterResource(id = img), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.LightGray)); Spacer(modifier = Modifier.height(8.dp)); Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor) } } }
                Spacer(modifier = Modifier.height(24.dp))

                // GỢI Ý
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { navController.navigate("fish_list/all") }) { Text("Gợi ý các loại cá", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor); Spacer(modifier = Modifier.weight(1f)); Icon(imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(24.dp), tint = textColor) }
                Spacer(modifier = Modifier.height(12.dp))
                LaunchedEffect(Unit) { while (true) { delay(2000); if (lazyRowState.firstVisibleItemIndex < 10) lazyRowState.animateScrollToItem(lazyRowState.firstVisibleItemIndex + 1) else lazyRowState.scrollToItem(0) } }
                LazyRow(state = lazyRowState, horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(largeFishList.take(20)) { fish -> FishItemCard(fish) { navController.navigate("fish_detail/${fish.id}") } } }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

// --- 7. MÀN HÌNH DANH SÁCH CÁ (CÓ LỌC) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishListScreen(navController: NavController, category: String) {
    // Lọc danh sách dựa trên tham số category
    val displayList = if (category == "all") largeFishList else largeFishList.filter { it.category.equals(category, ignoreCase = true) }
    // Tiêu đề hiển thị
    val titleText = if (category == "all") "Danh Sách Cá" else category

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(titleText, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        if (displayList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Không có loài cá nào trong mục này.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(displayList) { fish -> FishListItem(fish = fish, navController = navController) }
            }
        }
    }
}

// --- CÁC MÀN HÌNH KHÁC (GIỮ NGUYÊN) ---
@OptIn(ExperimentalMaterial3Api::class) @Composable fun CartScreen(navController: NavController, onGoToSearch: () -> Unit, onBackToSearch: () -> Unit) { BackHandler { onBackToSearch() }; val totalPrice = globalCartItems.sumOf { it.fish.priceInt * it.quantity.value }; Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Giỏ Hàng", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBackToSearch) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = MaterialTheme.colorScheme.onBackground)) }, bottomBar = { if (globalCartItems.isNotEmpty()) { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(8.dp), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) { Column(modifier = Modifier.padding(16.dp)) { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Tổng cộng:", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface); Text(formatCurrency(totalPrice), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Red) }; Spacer(modifier = Modifier.height(16.dp)); Button(onClick = { }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Thanh Toán (${globalCartItems.size} món)", fontWeight = FontWeight.Bold) } } } } }) { padding -> if (globalCartItems.isEmpty()) { Column(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.Gray); Spacer(modifier = Modifier.height(16.dp)); Text("Giỏ hàng trống", fontSize = 18.sp, color = Color.Gray); Spacer(modifier = Modifier.height(16.dp)); Button(onClick = onGoToSearch, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Mua sắm ngay") } } } else { LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(globalCartItems) { item -> CartItemRow(item, isCartItem = true) } } } } }
@Composable fun CartItemRow(item: CartItem, isCartItem: Boolean) { Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) { Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { AsyncImage(model = item.fish.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)), placeholder = painterResource(R.drawable.bg_dolphin)); Spacer(modifier = Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) { Text(item.fish.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface); Text(item.fish.category, fontSize = 12.sp, color = Color.Gray); Spacer(modifier = Modifier.height(4.dp)); Text(item.fish.price, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }; if (isCartItem) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = { if (item.quantity.value > 1) item.quantity.value-- }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = Color.Gray) }; Text("${item.quantity.value}", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface); IconButton(onClick = { item.quantity.value++ }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary) } }; Spacer(modifier = Modifier.height(4.dp)); TextButton(onClick = { globalCartItems.remove(item) }) { Text("Xóa", color = Color.Red, fontSize = 12.sp) } } } } } }
@Composable fun SearchScreen(navController: NavController) { var searchText by rememberSaveable { mutableStateOf("") }; val searchResults by remember { derivedStateOf { if (searchText.isBlank()) emptyList() else largeFishList.filter { it.name.contains(searchText, ignoreCase = true) } } }; Column(modifier = Modifier.fillMaxSize().padding(16.dp)) { Text("Khám Phá", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 16.dp)); OutlinedTextField(value = searchText, onValueChange = { searchText = it }, placeholder = { Text("Nhập tên cá...") }, leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface)); Spacer(modifier = Modifier.height(16.dp)); if (searchText.isEmpty()) { Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.LightGray); Text("Nhập tên cá để tìm kiếm", color = Color.Gray) } } else { if (searchResults.isEmpty()) { Text("Không tìm thấy kết quả.", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally)) } else { LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) { items(searchResults) { fish -> FishListItem(fish = fish, navController = navController) } } } } } }
@OptIn(ExperimentalMaterial3Api::class) @Composable fun ProfileScreen(navController: NavController) { var showAccountMenu by remember { mutableStateOf(false) }; var showSettingsMenu by remember { mutableStateOf(false) }; var showEditDialog by remember { mutableStateOf(false) }; var isPrivateAccount by remember { mutableStateOf(false) }; val isDarkTheme = LocalThemeState.current; val context = LocalContext.current; if (showEditDialog) { var tempName by remember { mutableStateOf(globalProfileName) }; var tempBio by remember { mutableStateOf(globalProfileBio) }; AlertDialog(onDismissRequest = { showEditDialog = false }, title = { Text("Chỉnh sửa hồ sơ") }, text = { Column { OutlinedTextField(value = tempName, onValueChange = { tempName = it }, label = { Text("Tên hiển thị") }); Spacer(modifier = Modifier.height(8.dp)); OutlinedTextField(value = tempBio, onValueChange = { tempBio = it }, label = { Text("Tiểu sử") }) } }, confirmButton = { Button(onClick = { globalProfileName = tempName; globalProfileBio = tempBio; showEditDialog = false }) { Text("Lưu") } }, dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Hủy") } }) }; Scaffold(topBar = { CenterAlignedTopAppBar(colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = MaterialTheme.colorScheme.onBackground), title = { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { showAccountMenu = true }) { Text(globalProfileName, fontWeight = FontWeight.Bold, fontSize = 20.sp); Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(20.dp)); DropdownMenu(expanded = showAccountMenu, onDismissRequest = { showAccountMenu = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) { DropdownMenuItem(text = { Text("Thêm tài khoản", color = MaterialTheme.colorScheme.onSurface) }, onClick = { showAccountMenu = false; navController.navigate("login") }, leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }); DropdownMenuItem(text = { Text("Đăng xuất", color = Color.Red) }, onClick = { showAccountMenu = false; navController.navigate("welcome") }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red) }) } } }, actions = { IconButton(onClick = { val newPost = UserPost(System.currentTimeMillis().toInt(), largeFishList.random().imageUrl); globalMyPosts.add(0, newPost) }) { Icon(Icons.Outlined.AddBox, contentDescription = "Add Post", modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onBackground) }; Box { IconButton(onClick = { showSettingsMenu = true }) { Icon(Icons.Outlined.Menu, contentDescription = "Menu", modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onBackground) }; DropdownMenu(expanded = showSettingsMenu, onDismissRequest = { showSettingsMenu = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) { DropdownMenuItem(text = { Text(if(isPrivateAccount) "Tắt Riêng tư" else "Bật Riêng tư") }, onClick = { isPrivateAccount = !isPrivateAccount }, leadingIcon = { Icon(if(isPrivateAccount) Icons.Filled.Lock else Icons.Outlined.LockOpen, contentDescription = null) }); DropdownMenuItem(text = { Text(if(isDarkTheme.value) "Chế độ Sáng" else "Chế độ Tối") }, onClick = { isDarkTheme.value = !isDarkTheme.value; val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE); prefs.edit().putBoolean("is_dark_mode", isDarkTheme.value).apply() }, leadingIcon = { Icon(if(isDarkTheme.value) Icons.Filled.LightMode else Icons.Filled.DarkMode, contentDescription = null) }); HorizontalDivider(); DropdownMenuItem(text = { Text("Đăng xuất", color = Color.Red) }, onClick = { navController.navigate("welcome") }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red) }) } } }) }) { padding -> Column(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) { Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Image(painter = painterResource(id = R.drawable.bg_dolphin), contentDescription = "Avatar", contentScale = ContentScale.Crop, modifier = Modifier.size(86.dp).clip(CircleShape).border(1.dp, Color.Gray, CircleShape)); Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) { ProfileStatItem("${globalMyPosts.size}", "Posts"); ProfileStatItem("0", "Followers"); ProfileStatItem("0", "Following") } }; Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) { Text(globalProfileName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground); if (isPrivateAccount) Text("🔒 Tài khoản riêng tư", fontSize = 13.sp, color = Color.Gray); Text(globalProfileBio, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground) }; Button(onClick = { showEditDialog = true }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(34.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isDarkTheme.value) Color(0xFF333333) else Color(0xFFEFEFEF), contentColor = MaterialTheme.colorScheme.onBackground), contentPadding = PaddingValues(0.dp)) { Text("Edit profile", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }; Spacer(modifier = Modifier.height(10.dp)); Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.GridOn, contentDescription = "Grid", modifier = Modifier.padding(10.dp).size(28.dp), tint = MaterialTheme.colorScheme.onBackground); HorizontalDivider(color = MaterialTheme.colorScheme.onBackground, thickness = 1.5.dp) }; if (globalMyPosts.isEmpty()) { Column(modifier = Modifier.fillMaxSize().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(60.dp).padding(bottom = 10.dp), tint = Color.Gray); Text("Chưa có bài viết nào", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground); Spacer(modifier = Modifier.height(20.dp)); Button(onClick = { val newPost = UserPost(System.currentTimeMillis().toInt(), largeFishList.random().imageUrl); globalMyPosts.add(0, newPost) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Tạo bài viết đầu tiên") } } } else { LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(0.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) { items(globalMyPosts) { post -> AsyncImage(model = post.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.aspectRatio(1f).clickable { navController.navigate("post_detail/${post.id}") }) } } } } } }
@Composable fun FishItemCard(fish: FishProduct, onClick: () -> Unit) { Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.width(140.dp).clickable { onClick() }) { Column { Box { AsyncImage(model = fish.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(100.dp), placeholder = painterResource(R.drawable.bg_dolphin)); val isFav = favoriteFishIds.contains(fish.id); Icon(imageVector = if(isFav) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "Like", tint = if(isFav) Color.Red else Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(24.dp).clickable { toggleFavorite(fish.id) }) }; Column(modifier = Modifier.padding(8.dp)) { Text(text = fish.category, fontSize = 10.sp, color = Color.Gray); Text(text = fish.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = MaterialTheme.colorScheme.onSurface); Spacer(modifier = Modifier.height(4.dp)); Text(text = fish.price, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) } } } }
@OptIn(ExperimentalMaterial3Api::class) @Composable fun PostDetailScreen(navController: NavController, postId: Int) { val post = globalMyPosts.find { it.id == postId }; var commentText by remember { mutableStateOf("") }; if (post == null) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Bài viết không tồn tại") } } else { Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Bài viết", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, actions = { IconButton(onClick = { globalMyPosts.remove(post); navController.popBackStack() }) { Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.Red) } }) }, bottomBar = { Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(8.dp), verticalAlignment = Alignment.CenterVertically) { OutlinedTextField(value = commentText, onValueChange = { commentText = it }, placeholder = { Text("Thêm bình luận...") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp)); IconButton(onClick = { if (commentText.isNotBlank()) { post.comments.add(Comment(globalProfileName, commentText)); commentText = "" } }) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary) } } }) { padding -> Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) { Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Image(painter = painterResource(id = R.drawable.bg_dolphin), contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop); Spacer(modifier = Modifier.width(12.dp)); Text(globalProfileName, fontWeight = FontWeight.Bold) }; AsyncImage(model = post.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().aspectRatio(1f)); Row(modifier = Modifier.padding(16.dp)) { val isLiked = post.isLiked.value; Icon(imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "Like", tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(28.dp).clickable { post.isLiked.value = !post.isLiked.value }); Spacer(modifier = Modifier.width(16.dp)); Icon(Icons.Outlined.ModeComment, contentDescription = "Comment", modifier = Modifier.size(28.dp)); Spacer(modifier = Modifier.width(16.dp)); Icon(Icons.Outlined.Send, contentDescription = "Share", modifier = Modifier.size(28.dp)) }; Column(modifier = Modifier.padding(horizontal = 16.dp)) { Text("${if(post.isLiked.value) "1" else "0"} lượt thích", fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(4.dp)); Row { Text(globalProfileName, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.width(8.dp)); Text("Hôm nay trời đẹp quá! 🐟") } }; Divider(modifier = Modifier.padding(vertical = 12.dp)); Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) { Text("Bình luận:", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp)); if (post.comments.isEmpty()) { Text("Chưa có bình luận nào.", color = Color.Gray, fontSize = 14.sp) } else { post.comments.forEach { comment -> Row(modifier = Modifier.padding(vertical = 4.dp)) { Text(comment.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp); Spacer(modifier = Modifier.width(8.dp)); Text(comment.content, fontSize = 14.sp) } } }; Spacer(modifier = Modifier.height(60.dp)) } } } } }
@OptIn(ExperimentalMaterial3Api::class) @Composable fun FavoritesScreen(navController: NavController) { val favList = largeFishList.filter { favoriteFishIds.contains(it.id) }; Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Đã Yêu Thích", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }) }) { padding -> if (favList.isEmpty()) Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Bạn chưa thả tim con cá nào!", color = Color.Gray) } else LazyColumn(modifier = Modifier.padding(padding).background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { items(favList) { fish -> FishListItem(fish = fish, navController = navController) } } } }
@Composable fun FishListItem(fish: FishProduct, navController: NavController) { Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).clickable { navController.navigate("fish_detail/${fish.id}") }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { AsyncImage(model = fish.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(70.dp).clip(CircleShape), placeholder = painterResource(R.drawable.bg_dolphin)); Spacer(modifier = Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) { Row(verticalAlignment = Alignment.CenterVertically) { val isFav = favoriteFishIds.contains(fish.id); Icon(imageVector = if(isFav) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null, modifier = Modifier.size(20.dp).clickable { toggleFavorite(fish.id) }, tint = if(isFav) Color.Red else Color.Gray); Spacer(modifier = Modifier.width(8.dp)); Text(fish.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface) }; Text(fish.habitat, fontSize = 12.sp, color = Color.Gray); Spacer(modifier = Modifier.height(8.dp)); Text(fish.price, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface) } } }
@Composable fun FishDetailScreen(navController: NavController, fishId: Int) { val fish = largeFishList.find { it.id == fishId } ?: largeFishList[0]; val context = LocalContext.current; Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) { Box { AsyncImage(model = fish.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(300.dp), placeholder = painterResource(R.drawable.bg_dolphin)); IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.padding(16.dp).background(Color.White.copy(alpha = 0.5f), CircleShape)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black) } }; Column(modifier = Modifier.padding(24.dp)) { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(fish.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); val isFav = favoriteFishIds.contains(fish.id); Icon(imageVector = if(isFav) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "Like", tint = if(isFav) Color.Red else Color.Gray, modifier = Modifier.size(32.dp).clickable { toggleFavorite(fish.id) }) }; Text(fish.price, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Red); Spacer(modifier = Modifier.height(8.dp)); Text(fish.category, fontSize = 16.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic); Spacer(modifier = Modifier.height(24.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(24.dp)); DetailItem("🌍 Sống ở đâu", fish.habitat); DetailItem("⚖️ Cân nặng tối đa", fish.maxWeight); DetailItem("🍣 Thích ăn gì", fish.diet); Spacer(modifier = Modifier.height(40.dp)); Button(onClick = { addToCart(fish); Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Thêm vào giỏ hàng") } } } }
@Composable fun DetailItem(label: String, value: String) { Column(modifier = Modifier.padding(bottom = 16.dp)) { Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground); Spacer(modifier = Modifier.height(4.dp)); Text(value, fontSize = 16.sp, color = Color.Gray) } }
@Composable fun ProfileStatItem(value: String, label: String) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground); Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground) } }