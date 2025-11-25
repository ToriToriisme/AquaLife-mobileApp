package com.example.aqualife.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.aqualife.R
import com.example.aqualife.ui.components.SkeletonGrid
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.aqualife.utils.FormatUtils
import com.example.aqualife.ui.viewmodel.HomeDisplayState
import com.example.aqualife.data.local.entity.FishEntity
import com.example.aqualife.ui.viewmodel.FavoriteViewModel
import java.io.File

// --- 1. MODEL DỮ LIỆU ---
data class FishProduct(
    val id: Int, 
    val name: String, 
    val price: String, 
    val priceInt: Int, 
    val imageUrl: String,
    val category: String, 
    val habitat: String, 
    val maxWeight: String, 
    val diet: String,
    val entityId: String = ""  // Real database ID for navigation
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

// --- 2. DỮ LIỆU ---
// NOTE: generateRealFishList() đã được thay thế bởi FishSeedData.generateRealFishData()
// Dữ liệu cá giờ được lấy từ Room Database thông qua FishRepository
// Hàm này được giữ lại để tương thích ngược nếu có code cũ còn sử dụng
@Deprecated("Use FishRepository.getAllFish() instead. Data is now loaded from Room Database via FishSeedData.")
fun generateRealFishList(): List<FishProduct> {
    // Return empty list - data should come from database
    return emptyList()
}

// --- 3. GLOBAL STATE ---
// NOTE: largeFishList không còn được sử dụng. Dữ liệu cá được lấy từ database qua ViewModel
@Deprecated("Use HomeViewModel.allFish or HomeViewModel.homeFishList instead")
val largeFishList = emptyList<FishProduct>()
val globalMyPosts = mutableStateListOf<UserPost>()

// DỮ LIỆU BANNER (4 LOẠI CÁ) - Using picsum.photos for stable images
val banners = listOf(
    BannerItem("Cá Biển", "https://picsum.photos/seed/banner_sea/800/400", "Cá biển"),
    BannerItem("Cá Sông", "https://picsum.photos/seed/banner_river/800/400", "Cá sông"),
    BannerItem("Cá Nước Lợ", "https://picsum.photos/seed/banner_brackish/800/400", "Cá nước lợ"),
    BannerItem("Cá Cảnh", "https://picsum.photos/seed/banner_pet/800/400", "Cá cảnh")
)

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
        
        // Get notification count
        val notificationViewModel: com.example.aqualife.ui.viewmodel.NotificationViewModel = hiltViewModel()
        val unreadCount by notificationViewModel.unreadCount.collectAsState()
        
        // Get cart count
        val cartViewModel: com.example.aqualife.ui.viewmodel.CartViewModel = hiltViewModel()
        val cartItems by cartViewModel.cartItems.collectAsState()
        
        Scaffold(
            bottomBar = { 
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) { 
                    items.forEachIndexed { index, item -> 
                        NavigationBarItem(
                            icon = { 
                                when {
                                    index == 2 && cartItems.isNotEmpty() -> {
                                        BadgedBox(badge = { Badge { Text("${cartItems.size}") } }) { 
                                            Icon(icons[index], contentDescription = item) 
                                        }
                                    }
                                    index == 3 && unreadCount > 0 -> {
                                        BadgedBox(badge = { Badge { Text("$unreadCount") } }) { 
                                            Icon(icons[index], contentDescription = item) 
                                        }
                                    }
                                    else -> Icon(icons[index], contentDescription = item)
                                }
                            }, 
                            selected = selectedItem == index, 
                            onClick = { selectedItem = index }, 
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary, 
                                unselectedIconColor = Color.Gray, 
                                indicatorColor = Color.Transparent
                            )
                        ) 
                    } 
                } 
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).background(MaterialTheme.colorScheme.background)) {
                when (selectedItem) {
                    0 -> HomeScreenContent(navController, onGoToCart = { selectedItem = 2 })
                    1 -> SearchScreen(navController)
                    2 -> CartScreen(navController, onGoToSearch = { selectedItem = 1 }, onBackToSearch = { selectedItem = 1 }, viewModel = cartViewModel)
                    3 -> NotificationsScreen(navController, notificationViewModel)
                    4 -> ProfileScreen(navController)
                    else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                        Text("Chức năng đang phát triển", color = MaterialTheme.colorScheme.onBackground) 
                    }
                }
            }
        }
    }
}

private fun createTempImageUri(context: Context): Uri? {
    return try {
        val tempFile = File(context.cacheDir, "post_${System.currentTimeMillis()}.jpg")
        if (!tempFile.exists()) {
            tempFile.createNewFile()
        }
        FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
    } catch (_: Exception) {
        null
    }
}

private fun addPostFromUri(uri: Uri) {
    globalMyPosts.add(0, UserPost(System.currentTimeMillis().toInt(), uri.toString()))
}

// --- 6. HOME SCREEN (BANNER TỰ TRƯỢT & LỌC CÁ) ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(
    navController: NavController, 
    onGoToCart: () -> Unit, 
    modifier: Modifier = Modifier,
    viewModel: com.example.aqualife.ui.viewmodel.HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    val lazyRowState = rememberLazyListState()
    val textColor = MaterialTheme.colorScheme.onBackground
    val bgColor = MaterialTheme.colorScheme.background
    val focusManager = LocalFocusManager.current
    
    val allFish by viewModel.allFish.collectAsState()
    val displayState by viewModel.displayState.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val favoriteEntities by favoriteViewModel.favoriteFish.collectAsState()

    val homeFishProducts = remember(allFish) { allFish.map { it.toFishProduct() } }
    val allFishProducts = remember(allFish) { allFish.map { it.toFishProduct() } }
    val favoriteProducts = remember(favoriteEntities) { favoriteEntities.map { it.toFishProduct() } }
    val favoriteIdSet = remember(favoriteEntities) { favoriteEntities.map { it.id }.toSet() }
    
    var searchField by rememberSaveable(stateSaver = TextFieldValue.Saver) { 
        mutableStateOf(TextFieldValue("", TextRange.Zero)) 
    }
    val searchText = searchField.text
    // Helper function to normalize Vietnamese text for accent-insensitive search
    fun normalizeVietnamese(text: String): String {
        return text.lowercase()
            .replace("á", "a").replace("à", "a").replace("ả", "a").replace("ã", "a").replace("ạ", "a")
            .replace("ă", "a").replace("ắ", "a").replace("ằ", "a").replace("ẳ", "a").replace("ẵ", "a").replace("ặ", "a")
            .replace("â", "a").replace("ấ", "a").replace("ầ", "a").replace("ẩ", "a").replace("ẫ", "a").replace("ậ", "a")
            .replace("é", "e").replace("è", "e").replace("ẻ", "e").replace("ẽ", "e").replace("ẹ", "e")
            .replace("ê", "e").replace("ế", "e").replace("ề", "e").replace("ể", "e").replace("ễ", "e").replace("ệ", "e")
            .replace("í", "i").replace("ì", "i").replace("ỉ", "i").replace("ĩ", "i").replace("ị", "i")
            .replace("ó", "o").replace("ò", "o").replace("ỏ", "o").replace("õ", "o").replace("ọ", "o")
            .replace("ô", "o").replace("ố", "o").replace("ồ", "o").replace("ổ", "o").replace("ỗ", "o").replace("ộ", "o")
            .replace("ơ", "o").replace("ớ", "o").replace("ờ", "o").replace("ở", "o").replace("ỡ", "o").replace("ợ", "o")
            .replace("ú", "u").replace("ù", "u").replace("ủ", "u").replace("ũ", "u").replace("ụ", "u")
            .replace("ư", "u").replace("ứ", "u").replace("ừ", "u").replace("ử", "u").replace("ữ", "u").replace("ự", "u")
            .replace("ý", "y").replace("ỳ", "y").replace("ỷ", "y").replace("ỹ", "y").replace("ỵ", "y")
            .replace("đ", "d")
    }
    
    val searchResults = remember(searchText, allFishProducts) {
        if (searchText.isBlank()) emptyList() 
        else {
            val normalizedSearch = normalizeVietnamese(searchText)
            allFishProducts.filter { fish ->
                val normalizedName = normalizeVietnamese(fish.name)
                normalizedName.contains(normalizedSearch) || fish.name.contains(searchText, ignoreCase = true)
            }
        }
    }
    
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(pageCount = { banners.size })
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % banners.size)
        }
    }
    
    // Auto-request focus when screen loads (for keyboard to show)
    LaunchedEffect(Unit) {
        delay(300) // Small delay to ensure screen is ready
        searchFocusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchField,
            onValueChange = { newValue ->
                // Preserve composition state - use compositionText if available
                searchField = if (newValue.composition != null) {
                    // Keep composition state for IME input
                    newValue
                } else {
                    // Normal update
                    TextFieldValue(newValue.text, newValue.selection, newValue.composition)
                }
            },
            placeholder = { Text("Tìm kiếm...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = { 
                        viewModel.addSearchHistory(searchText.trim())
                        // Clear properly - reset to empty with proper TextRange
                        searchField = TextFieldValue("", TextRange.Zero)
                        // Keep focus - simplified
                        coroutineScope.launch {
                            delay(200)
                            searchFocusRequester.requestFocus()
                        }
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .focusRequester(searchFocusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        // Show keyboard when focused - simplified
                        coroutineScope.launch {
                            delay(150)
                            keyboardController?.show()
                        }
                    }
                },
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    viewModel.addSearchHistory(searchText.trim())
                    focusManager.clearFocus()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (searchText.isNotBlank()) {
            SearchResultSection(
                results = searchResults,
                navController = navController,
                textColor = textColor,
                favoriteIds = favoriteIdSet,
                onToggleFavorite = { favoriteViewModel.toggleFavorite(it) }
            )
        } else {
            when (displayState) {
                is HomeDisplayState.FilteredView -> {
                    val selectedCategory = (displayState as HomeDisplayState.FilteredView).category
                    FilteredHomeSection(
                        category = selectedCategory,
                        fishList = homeFishProducts,
                        navController = navController,
                        onReset = { viewModel.resetToDefaultView() },
                        favoriteIds = favoriteIdSet,
                        onToggleFavorite = { favoriteViewModel.toggleFavorite(it) }
                    )
                }
                HomeDisplayState.DefaultView -> {
                    DefaultHomeSection(
                        fishList = homeFishProducts,
                        isLoading = false,
                        navController = navController,
                        onGoToCart = onGoToCart,
                        lazyRowState = lazyRowState,
                        pagerState = pagerState,
                        textColor = textColor,
                        onCategorySelected = { viewModel.selectCategoryFilter(it) },
                        favoriteIds = favoriteIdSet,
                        onToggleFavorite = { favoriteViewModel.toggleFavorite(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultSection(
    results: List<FishProduct>,
    navController: NavController,
    textColor: Color,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit
) {
    if (results.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy loại cá nào.", color = Color.Gray)
        }
    } else {
        Text(
            "Kết quả tìm kiếm:",
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { fish ->
                FishListItem(
                    fish = fish,
                    navController = navController,
                    isFavorite = favoriteIds.contains(fish.entityId.ifEmpty { fish.id.toString() }),
                    onToggleFavorite = onToggleFavorite
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
private fun DefaultHomeSection(
    fishList: List<FishProduct>,
    isLoading: Boolean,
    navController: NavController,
    onGoToCart: () -> Unit,
    lazyRowState: androidx.compose.foundation.lazy.LazyListState,
    pagerState: androidx.compose.foundation.pager.PagerState,
    textColor: Color,
    onCategorySelected: (String) -> Unit,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit
) {
                val menuTabs = listOf("♥ Yêu Thích", "🕒 Lịch sử", "👤 Theo dõi", "📦 Orders")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(menuTabs) { tab ->
                Text(
                    text = tab,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray,
                    modifier = Modifier.clickable {
                        when {
                            tab.contains("Yêu Thích") -> navController.navigate("favorites")
                            tab.contains("Orders") -> onGoToCart()
                        }
                    }
                )
            }
        }
                Spacer(modifier = Modifier.height(16.dp))

                HorizontalPager(
                    state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                ) { page ->
                    val banner = banners[page]
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                    .clickable { navController.navigate("fish_list/${banner.categoryKey}") }
                    ) {
                        Box {
                            AsyncImage(
                                model = banner.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                                Text(banner.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
        Text("Loại Cá", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
        Spacer(modifier = Modifier.height(12.dp))
        CategoryIconsRow(
            onCategorySelected = onCategorySelected,
            onShowAll = { onCategorySelected("Tất cả") }
        )
                Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("fish_list/all") }
        ) {
            Text("Gợi ý các loại cá", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = textColor
            )
        }
                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    SkeletonGrid()
                } else {
            LaunchedEffect(Unit) {
                while (true) {
                    delay(2000)
                    if (lazyRowState.firstVisibleItemIndex < 10) {
                        lazyRowState.animateScrollToItem(lazyRowState.firstVisibleItemIndex + 1)
                    } else {
                        lazyRowState.scrollToItem(0)
                    }
                }
            }
            LazyRow(
                state = lazyRowState,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(fishList.take(20).size) { index ->
                    val fish = fishList[index]
                    val favoriteKey = if (fish.entityId.isNotEmpty()) fish.entityId else fish.id.toString()
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(index) {
                        delay(index * 50L)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        FishItemCard(
                            fish = fish,
                            onClick = {
                                val fishId = if (fish.entityId.isNotEmpty()) fish.entityId else "sea_01"
                                navController.navigate("fish_detail/$fishId")
                            },
                            isFavorite = favoriteIds.contains(favoriteKey),
                            onToggleFavorite = { onToggleFavorite(favoriteKey) }
                        )
                    }
                }
            }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

@Composable
private fun FilteredHomeSection(
    category: String,
    fishList: List<FishProduct>,
    navController: NavController,
    onReset: () -> Unit,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Kết quả lọc: $category",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        TextButton(onClick = onReset) {
            Text("← Xem tất cả sản phẩm")
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (fishList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không có sản phẩm trong danh mục này.", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(fishList) { fish ->
                    val favoriteKey = if (fish.entityId.isNotEmpty()) fish.entityId else fish.id.toString()
                    FishItemCard(
                        fish = fish,
                        onClick = {
                            val fishId = if (fish.entityId.isNotEmpty()) fish.entityId else "sea_01"
                            navController.navigate("fish_detail/$fishId")
                        },
                        isFavorite = favoriteIds.contains(favoriteKey),
                        onToggleFavorite = { onToggleFavorite(favoriteKey) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryChips(
    history: List<String>,
    onHistorySelected: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        history.forEachIndexed { index, term ->
            var isPressed by remember { mutableStateOf(false) }
            val chipScale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                ),
                label = "historyChipScale"
            )
            LaunchedEffect(index) {
                delay(index * 30L)
            }
            AssistChip(
                onClick = { 
                    isPressed = true
                    onHistorySelected(term)
                    coroutineScope.launch {
                        delay(100)
                        isPressed = false
                    }
                },
                label = { 
                    Text(
                        term, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.scale(chipScale)
                    ) 
                },
                modifier = Modifier.scale(chipScale)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FavoriteChipRow(
    favorites: List<FishProduct>,
    onChipClick: (FishProduct) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    Text(
        "Yêu thích của bạn", 
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        favorites.forEachIndexed { index, fish ->
            var isPressed by remember { mutableStateOf(false) }
            val chipScale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                ),
                label = "chipScale"
            )
            LaunchedEffect(index) {
                delay(index * 50L)
            }
            SuggestionChip(
                onClick = { 
                    isPressed = true
                    onChipClick(fish)
                    coroutineScope.launch {
                        delay(100)
                        isPressed = false
                    }
                },
                label = { 
                    Text(
                        fish.name, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.scale(chipScale)
                    ) 
                },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.scale(chipScale)
            )
        }
    }
}

@Composable
private fun CategoryIconsRow(
    onCategorySelected: (String) -> Unit,
    onShowAll: () -> Unit
) {
    val categories = listOf(
        "Cá biển" to R.drawable.img_ca_nuoc_bien,
        "Cá sông" to R.drawable.img_ca_song,
        "Cá nước lợ" to R.drawable.img_ca_nuoc_lo,
        "Cá cảnh" to R.drawable.img_ca_kieng
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            categories.forEach { (name, drawable) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(72.dp)
                        .clickable { onCategorySelected(name) }
                ) {
                    Image(
                        painter = painterResource(id = drawable),
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onShowAll) {
                Text("Xem tất cả")
            }
        }
    }
}

private fun FishEntity.toFishProduct(): FishProduct {
    return FishProduct(
        id = id.hashCode(),
        name = name,
        price = FormatUtils.formatCurrency(priceInt),
        priceInt = priceInt,
        imageUrl = imageUrl,
        category = category,
        habitat = habitat,
        maxWeight = maxWeight,
        diet = diet,
        entityId = id
    )
}

// --- 7. MÀN HÌNH DANH SÁCH CÁ (CÓ LỌC) - UPDATED TO USE DATABASE ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishListScreen(
    navController: NavController, 
    category: String,
    viewModel: com.example.aqualife.ui.viewmodel.HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    val titleText = if (category == "all") "Danh Sách Cá" else category
    
    // Get data from database based on category
    val fishEntities by if (category == "all") {
        viewModel.allFish.collectAsState()
    } else {
        viewModel.getFishByCategory(category).collectAsState(initial = emptyList())
    }
    
    // Convert to FishProduct for UI compatibility
    val displayList = fishEntities.map { entity ->
        FishProduct(
            id = entity.id.hashCode(), // Temporary Int ID for UI
            name = entity.name,
            price = FormatUtils.formatCurrency(entity.priceInt),
            priceInt = entity.priceInt,
            imageUrl = entity.imageUrl,
            category = entity.category,
            habitat = entity.habitat,
            maxWeight = entity.maxWeight,
            diet = entity.diet,
            entityId = entity.id // Real DB ID for navigation
        )
    }

    val favoriteIds by favoriteViewModel.favoriteFish.collectAsState()
    val favoriteIdSet = remember(favoriteIds) { favoriteIds.map { it.id }.toSet() }

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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Đang tải dữ liệu...", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(displayList) { fish ->
                    FishListItem(
                        fish = fish,
                        navController = navController,
                        isFavorite = favoriteIdSet.contains(fish.entityId.ifEmpty { fish.id.toString() }),
                        onToggleFavorite = { favoriteViewModel.toggleFavorite(it) }
                    )
                }
            }
        }
    }
}

// --- CÁC MÀN HÌNH KHÁC (GIỮ NGUYÊN) ---
@OptIn(ExperimentalMaterial3Api::class) 
@Composable 
fun CartScreen(
    navController: NavController, 
    onGoToSearch: () -> Unit, 
    onBackToSearch: () -> Unit,
    viewModel: com.example.aqualife.ui.viewmodel.CartViewModel = hiltViewModel()
) { 
    BackHandler { onBackToSearch() }
    
    val cartItems by viewModel.cartItems.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    
    Scaffold(
        topBar = { 
            CenterAlignedTopAppBar(
                title = { Text("Giỏ Hàng", fontWeight = FontWeight.Bold) }, 
                navigationIcon = { 
                    IconButton(onClick = onBackToSearch) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") 
                    } 
                }, 
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background, 
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            ) 
        }, 
        bottomBar = { 
            if (cartItems.isNotEmpty()) { 
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
                    elevation = CardDefaults.cardElevation(8.dp), 
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) { 
                    Column(modifier = Modifier.padding(16.dp)) { 
                        Row(
                            modifier = Modifier.fillMaxWidth(), 
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) { 
                            Text("Tổng cộng:", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(FormatUtils.formatCurrency(totalPrice.toInt()), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Red) 
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate("payment") }, 
                            modifier = Modifier.fillMaxWidth().height(50.dp), 
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) { 
                            Text("Thanh Toán (${cartItems.size} món)", fontWeight = FontWeight.Bold) 
                        } 
                    } 
                } 
            } 
        }
    ) { padding -> 
        if (cartItems.isEmpty()) { 
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background), 
                horizontalAlignment = Alignment.CenterHorizontally, 
                verticalArrangement = Arrangement.Center
            ) { 
                Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Giỏ hàng trống", fontSize = 18.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onGoToSearch, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { 
                    Text("Mua sắm ngay") 
                } 
            } 
        } else { 
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background), 
                contentPadding = PaddingValues(16.dp), 
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) { 
                items(cartItems) { item -> 
                    CartItemRow(
                        item = item, 
                        isCartItem = true,
                        onUpdateQuantity = { fishId, quantity -> viewModel.updateQuantity(fishId, quantity) },
                        onRemove = { fishId -> viewModel.removeFromCart(fishId) }
                    ) 
                } 
            } 
        } 
    } 
}
@Composable 
fun CartItemRow(
    item: com.example.aqualife.ui.viewmodel.CartItemUi, 
    isCartItem: Boolean,
    onUpdateQuantity: (String, Int) -> Unit = { _, _ -> },
    onRemove: (String) -> Unit = {}
) { 
    Card(
        shape = RoundedCornerShape(12.dp), 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
        elevation = CardDefaults.cardElevation(2.dp)
    ) { 
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(), 
            verticalAlignment = Alignment.CenterVertically
        ) { 
            AsyncImage(
                model = item.fish.imageUrl, 
                contentDescription = null, 
                contentScale = ContentScale.Crop, 
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)), 
                placeholder = painterResource(R.drawable.bg_dolphin)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) { 
                Text(item.fish.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(item.fish.category, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(FormatUtils.formatCurrency(item.fish.priceInt), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) 
            }
            if (isCartItem) { 
                Column(horizontalAlignment = Alignment.CenterHorizontally) { 
                    Row(verticalAlignment = Alignment.CenterVertically) { 
                        IconButton(
                            onClick = { 
                                if (item.quantity > 1) {
                                    onUpdateQuantity(item.fish.id.toString(), item.quantity - 1)
                                }
                            }, 
                            modifier = Modifier.size(30.dp)
                        ) { 
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = Color.Gray) 
                        }
                        Text("${item.quantity}", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        IconButton(
                            onClick = { onUpdateQuantity(item.fish.id.toString(), item.quantity + 1) }, 
                            modifier = Modifier.size(30.dp)
                        ) { 
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary) 
                        } 
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(onClick = { onRemove(item.fish.id.toString()) }) { 
                        Text("Xóa", color = Color.Red, fontSize = 12.sp) 
                    } 
                } 
            } 
        } 
    } 
}
// --- 8. SEARCH SCREEN - UPDATED TO USE DATABASE ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable 
fun SearchScreen(
    navController: NavController,
    viewModel: com.example.aqualife.ui.viewmodel.HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    var searchField by rememberSaveable(stateSaver = TextFieldValue.Saver) { 
        mutableStateOf(TextFieldValue("", TextRange.Zero)) 
    }
    var selectedCategory by rememberSaveable { mutableStateOf("all") }
    var selectedSort by rememberSaveable { mutableStateOf("best_seller") }
    var discountOnly by rememberSaveable { mutableStateOf(false) }
    var minRating by rememberSaveable { mutableFloatStateOf(0f) }
    val history by viewModel.searchHistory.collectAsState()
    val favoriteEntities by favoriteViewModel.favoriteFish.collectAsState()
    val favoriteIdSet = remember(favoriteEntities) { favoriteEntities.map { it.id }.toSet() }
    val searchText = searchField.text

    val filteredFlow = remember(searchText, selectedCategory, selectedSort, discountOnly, minRating) {
        viewModel.getFilteredFish(
            query = searchText.trim(),
            category = selectedCategory,
            minPrice = 0.0,
            maxPrice = 200_000_000.0,
            minRating = if (minRating <= 0f) null else minRating,
            discountOnly = discountOnly,
            sortBy = selectedSort
        )
    }
    val searchEntities by filteredFlow.collectAsState(initial = emptyList())
    val searchResults = searchEntities.map { entity ->
        FishProduct(
            id = entity.id.hashCode(),
            name = entity.name,
            price = FormatUtils.formatCurrency(entity.priceInt),
            priceInt = entity.priceInt,
            imageUrl = entity.imageUrl,
            category = entity.category,
            habitat = entity.habitat,
            maxWeight = entity.maxWeight,
            diet = entity.diet,
            entityId = entity.id
        )
    }

    val categoryOptions = listOf("all", "Cá biển", "Cá sông", "Cá nước lợ", "Cá cảnh")
    val sortOptions = listOf(
        "best_seller" to "Bán chạy",
        "price_asc" to "Giá ↑",
        "price_desc" to "Giá ↓",
        "rating" to "Đánh giá"
    )
    val ratingOptions = listOf(
        0f to "Tất cả",
        4f to "⭐ 4+",
        4.5f to "⭐ 4.5+"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Khám Phá",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        val searchFocusRequester2 = remember { FocusRequester() }
        val keyboardController2 = LocalSoftwareKeyboardController.current
        val coroutineScope2 = rememberCoroutineScope()
        
        // Auto-request focus when SearchScreen loads
        LaunchedEffect(Unit) {
            delay(300)
            searchFocusRequester2.requestFocus()
        }
        
        OutlinedTextField(
            value = searchField,
            onValueChange = { newValue ->
                // Preserve composition state for IME input (Telex)
                searchField = if (newValue.composition != null) {
                    // Keep composition state for IME
                    newValue
                } else {
                    // Normal update - preserve selection
                    TextFieldValue(newValue.text, newValue.selection, newValue.composition)
                }
            },
            placeholder = { Text("Nhập tên cá...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(searchFocusRequester2)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        // Show keyboard when focused - simplified
                        coroutineScope2.launch {
                            delay(150)
                            keyboardController2?.show()
                        }
                    }
                },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    viewModel.addSearchHistory(searchText.trim())
                    focusManager.clearFocus()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            trailingIcon = {
                if (searchText.isNotBlank()) {
                    IconButton(onClick = { 
                        viewModel.addSearchHistory(searchText.trim())
                        // Clear properly - reset to empty with proper TextRange
                        searchField = TextFieldValue("", TextRange.Zero)
                        // Keep focus - simplified
                        coroutineScope2.launch {
                            delay(200)
                            searchFocusRequester2.requestFocus()
                        }
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Xóa")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        CategoryFilterRow(
            options = categoryOptions,
            selectedOption = selectedCategory,
            onOptionSelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = discountOnly,
                onClick = { discountOnly = !discountOnly },
                label = { Text("Đang giảm giá") }
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ratingOptions) { (value, label) ->
                    FilterChip(
                        selected = minRating == value,
                        onClick = { minRating = value },
                        label = { Text(label) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sortOptions) { (value, label) ->
                SuggestionChip(
                    onClick = { selectedSort = value },
                    label = { Text(label) },
                    icon = {
                        if (selectedSort == value) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
        }

        if (history.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Lịch sử tìm kiếm", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            HistoryChips(
                history = history,
                onHistorySelected = { term ->
                    searchField = TextFieldValue(term, TextRange(term.length))
                    viewModel.addSearchHistory(term)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Color.LightGray
                    )
                    Text(
                        if (searchText.isBlank()) "Nhập tên cá để tìm kiếm" else "Không tìm thấy kết quả.",
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults) { fish ->
                    FishListItem(
                        fish = fish,
                        navController = navController,
                        isFavorite = favoriteIdSet.contains(fish.entityId.ifEmpty { fish.id.toString() }),
                        onToggleFavorite = { favoriteViewModel.toggleFavorite(it) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class) 
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: com.example.aqualife.ui.viewmodel.AuthViewModel = hiltViewModel()
) {
    var showAccountMenu by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddPostSheet by remember { mutableStateOf(false) }
    var isPrivateAccount by remember { mutableStateOf(false) }
    val isDarkTheme = LocalThemeState.current
    val context = LocalContext.current
    val firebaseUser by authViewModel.currentUser.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    fun handleNewPost(uri: Uri) {
        addPostFromUri(uri)
        Toast.makeText(context, "Đã tạo bài viết mới.", Toast.LENGTH_SHORT).show()
    }
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraUri?.let { uri ->
                handleNewPost(uri)
            }
        }
        cameraUri = null
    }
    val pickMediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { handleNewPost(it) }
    }
    fun launchCameraAfterPermission() {
        val uri = createTempImageUri(context)
        if (uri != null) {
            cameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Không thể tạo file ảnh tạm.", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchGalleryAfterPermission() {
        pickMediaLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCameraAfterPermission()
        } else {
            Toast.makeText(context, "Cần quyền Camera để chụp ảnh.", Toast.LENGTH_SHORT).show()
        }
    }
    val galleryPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchGalleryAfterPermission()
        } else {
            Toast.makeText(context, "Cần quyền truy cập ảnh để chọn từ thư viện.", Toast.LENGTH_SHORT).show()
        }
    }

    fun startCameraFlow() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCameraAfterPermission()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun startGalleryFlow() {
        if (ContextCompat.checkSelfPermission(context, galleryPermission) == PackageManager.PERMISSION_GRANTED) {
            launchGalleryAfterPermission()
        } else {
            galleryPermissionLauncher.launch(galleryPermission)
        }
    }

    val displayName = userProfile?.displayName?.takeIf { it.isNotBlank() }
        ?: firebaseUser?.displayName
        ?: firebaseUser?.email
        ?: "AquaLife User"
    val bio = userProfile?.bio?.takeIf { it.isNotBlank() } ?: "Chưa có mô tả."
    val avatarUrl = userProfile?.avatarUrl?.takeIf { it.isNotBlank() }

    if (firebaseUser == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Vui lòng đăng nhập để xem hồ sơ.", color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("login") }) {
                    Text("Đăng nhập")
                }
            }
        }
        return
    }

    if (showEditDialog) {
        var tempName by remember { mutableStateOf(displayName) }
        var tempBio by remember { mutableStateOf(bio) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Chỉnh sửa hồ sơ") },
            text = {
                Column {
                    OutlinedTextField(value = tempName, onValueChange = { tempName = it }, label = { Text("Tên hiển thị") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = tempBio, onValueChange = { tempBio = it }, label = { Text("Tiểu sử") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    authViewModel.updateProfile(tempName.trim(), tempBio.trim())
                    showEditDialog = false
                }) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    if (showAddPostSheet) {
        AlertDialog(
            onDismissRequest = { showAddPostSheet = false },
            title = { Text("Tạo bài viết mới") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Chọn ảnh từ thư viện") },
                        supportingContent = { Text("Tải ảnh có sẵn trong máy của bạn") },
                        leadingContent = { Icon(Icons.Default.Image, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAddPostSheet = false
                                startGalleryFlow()
                            }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ListItem(
                        headlineContent = { Text("Chụp ảnh mới") },
                        supportingContent = { Text("Mở camera để chụp ảnh và đăng ngay") },
                        leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAddPostSheet = false
                                startCameraFlow()
                            }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddPostSheet = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showAccountMenu = true }
                    ) {
                        Text(displayName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(20.dp))
                        DropdownMenu(expanded = showAccountMenu, onDismissRequest = { showAccountMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Đăng xuất", color = Color.Red) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red) },
                                onClick = {
                                    showAccountMenu = false
                                    authViewModel.logout()
                                    navController.navigate("welcome")
                                }
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showAddPostSheet = true }) {
                        Icon(Icons.Outlined.AddBox, contentDescription = "Add Post", modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onBackground)
                    }
                    Box {
                        IconButton(onClick = { showSettingsMenu = true }) {
                            Icon(Icons.Outlined.Menu, contentDescription = "Menu", modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onBackground)
                        }
                        DropdownMenu(expanded = showSettingsMenu, onDismissRequest = { showSettingsMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(if (isPrivateAccount) "Tắt riêng tư" else "Bật riêng tư") },
                                leadingIcon = { Icon(if (isPrivateAccount) Icons.Filled.Lock else Icons.Outlined.LockOpen, contentDescription = null) },
                                onClick = { isPrivateAccount = !isPrivateAccount }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isDarkTheme.value) "Chế độ Sáng" else "Chế độ Tối") },
                                leadingIcon = { Icon(if (isDarkTheme.value) Icons.Filled.LightMode else Icons.Filled.DarkMode, contentDescription = null) },
                                onClick = {
                                    isDarkTheme.value = !isDarkTheme.value
                                    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                                    prefs.edit().putBoolean("is_dark_mode", isDarkTheme.value).apply()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.Gray, CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.bg_dolphin),
                        error = painterResource(R.drawable.bg_dolphin)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.bg_dolphin),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.Gray, CircleShape)
                    )
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileStatItem("${globalMyPosts.size}", "Posts")
                    ProfileStatItem("0", "Followers")
                    ProfileStatItem("0", "Following")
                }
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                if (isPrivateAccount) {
                    Text("🔒 Tài khoản riêng tư", fontSize = 13.sp, color = Color.Gray)
                }
                Text(bio, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
            }
            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(34.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkTheme.value) Color(0xFF333333) else Color(0xFFEFEFEF),
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Edit profile", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.GridOn, contentDescription = "Grid", modifier = Modifier.padding(10.dp).size(28.dp), tint = MaterialTheme.colorScheme.onBackground)
                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground, thickness = 1.5.dp)
            }
            if (globalMyPosts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(60.dp).padding(bottom = 10.dp), tint = Color.Gray)
                    Text("Chưa có bài viết nào", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = { showAddPostSheet = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                        Text("Tạo bài viết đầu tiên")
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(0.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(globalMyPosts) { post ->
                        AsyncImage(
                            model = post.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { navController.navigate("post_detail/${post.id}") }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun FishItemCard(
    fish: FishProduct,
    onClick: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFavorite) 8.dp else 2.dp),
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        Column {
            Box {
                AsyncImage(
                    model = fish.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    placeholder = painterResource(R.drawable.bg_dolphin),
                    error = painterResource(R.drawable.bg_dolphin)
                )
                var favoriteState by remember(isFavorite) { mutableStateOf(isFavorite) }
                LaunchedEffect(isFavorite) {
                    favoriteState = isFavorite
                }
                val favoriteScale by animateFloatAsState(
                    targetValue = if (favoriteState) 1.15f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "favoriteIconScale"
                )
                Icon(
                    imageVector = if (favoriteState) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (favoriteState) Color.Red else Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .scale(favoriteScale)
                        .clickable { 
                            favoriteState = !favoriteState
                            onToggleFavorite()
                        }
                )
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = fish.category, fontSize = 10.sp, color = Color.Gray)
                Text(
                    text = fish.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fish.price,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    navController: NavController,
    postId: Int,
    authViewModel: com.example.aqualife.ui.viewmodel.AuthViewModel = hiltViewModel()
) {
    val post = globalMyPosts.find { it.id == postId }
    var commentText by remember { mutableStateOf("") }
    val userProfile by authViewModel.userProfile.collectAsState()
    val firebaseUser by authViewModel.currentUser.collectAsState()
    val displayName = userProfile?.displayName?.takeIf { it.isNotBlank() }
        ?: firebaseUser?.displayName
        ?: firebaseUser?.email
        ?: "AquaLife User"

    if (post == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bài viết không tồn tại")
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bài viết", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        globalMyPosts.remove(post)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Thêm bình luận...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                )
                IconButton(onClick = {
                    if (commentText.isNotBlank()) {
                        post.comments.add(Comment(displayName, commentText.trim()))
                        commentText = ""
                    }
                }) {
                    Icon(Icons.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.bg_dolphin),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(displayName, fontWeight = FontWeight.Bold)
            }
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )
            Row(modifier = Modifier.padding(16.dp)) {
                val isLiked = post.isLiked.value
                Icon(
                    imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp).clickable { post.isLiked.value = !post.isLiked.value }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Outlined.ModeComment, contentDescription = "Comment", modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Filled.Send, contentDescription = "Share", modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("${if (post.isLiked.value) "1" else "0"} lượt thích", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(displayName, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hôm nay trời đẹp quá! 🐟")
                }
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Bình luận:", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                if (post.comments.isEmpty()) {
                    Text("Chưa có bình luận nào.", color = Color.Gray, fontSize = 14.sp)
                } else {
                    post.comments.forEach { comment ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(comment.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(comment.content, fontSize = 14.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) 
@Composable 
fun FavoritesScreen(
    navController: NavController,
    viewModel: com.example.aqualife.ui.viewmodel.FavoriteViewModel = hiltViewModel()
) { 
    val favoriteFish by viewModel.favoriteFish.collectAsState()
    
    // Convert FishEntity to FishProduct
    val favList = favoriteFish.map { entity ->
        FishProduct(
            id = entity.id.hashCode(),
            name = entity.name,
            price = FormatUtils.formatCurrency(entity.priceInt),
            priceInt = entity.priceInt,
            imageUrl = entity.imageUrl,
            category = entity.category,
            habitat = entity.habitat,
            maxWeight = entity.maxWeight,
            diet = entity.diet,
            entityId = entity.id // Real DB ID for navigation
        )
    }
    
    Scaffold(
        topBar = { 
            CenterAlignedTopAppBar(
                title = { Text("Đã Yêu Thích", fontWeight = FontWeight.Bold) }, 
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
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
        if (favList.isEmpty()) {
            Box(
                    modifier = Modifier.fillMaxSize(), 
                contentAlignment = Alignment.Center
            ) { 
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Bạn chưa thả tim con cá nào!", color = Color.Gray)
                }
            } 
        } else {
                // Add Favorite Chips at the top
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    FavoriteChipRow(
                        favorites = favList,
                        onChipClick = { fish ->
                            val fishId = if (fish.entityId.isNotEmpty()) fish.entityId else "sea_01"
                            navController.navigate("fish_detail/$fishId")
                        }
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // List of favorite items
            LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp), 
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) { 
                items(favList) { fish -> 
                        FishListItem(
                            fish = fish,
                            navController = navController,
                            isFavorite = true,
                            onToggleFavorite = { favoriteKey -> viewModel.toggleFavorite(favoriteKey) }
                        )
                    } 
                } 
            } 
        } 
    } 
}
@Composable 
fun FishListItem(
    fish: FishProduct, 
    navController: NavController,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit
) {
    val favoriteKey = if (fish.entityId.isNotEmpty()) fish.entityId else fish.id.toString()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .clickable { 
                val fishId = if (fish.entityId.isNotEmpty()) fish.entityId else fish.id.toString()
                navController.navigate("fish_detail/$fishId") 
            }
            .padding(12.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) { 
        AsyncImage(
            model = fish.imageUrl, 
            contentDescription = null, 
            contentScale = ContentScale.Crop, 
            modifier = Modifier.size(70.dp).clip(CircleShape), 
            placeholder = painterResource(R.drawable.bg_dolphin)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) { 
            Row(verticalAlignment = Alignment.CenterVertically) { 
                Icon(
                    imageVector = if(isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder, 
                    contentDescription = null, 
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onToggleFavorite(favoriteKey) }, 
                    tint = if(isFavorite) Color.Red else Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(fish.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface) 
            }
            Text(fish.habitat, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(fish.price, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface) 
        } 
    } 
}
// --- 11. FISH DETAIL SCREEN - UPDATED TO USE DATABASE ---
@Composable 
fun FishDetailScreen(
    navController: NavController, 
    fishId: String,
    viewModel: com.example.aqualife.ui.viewmodel.HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    cartViewModel: com.example.aqualife.ui.viewmodel.CartViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    val fishEntity by viewModel.allFish.collectAsState()
    val fish = fishEntity.find { it.id == fishId }
    val favoriteFish by favoriteViewModel.favoriteFish.collectAsState()
    val isFavorite = remember(fishId, favoriteFish) { 
        favoriteFish.any { it.id == fishId }
    }
    
    if (fish == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Đang tải...", color = Color.Gray)
            }
        }
        return
    }
    
    val fishProduct = FishProduct(
        id = fish.id.hashCode(),
        name = fish.name,
        price = FormatUtils.formatCurrency(fish.priceInt),
        priceInt = fish.priceInt,
        imageUrl = fish.imageUrl,
        category = fish.category,
        habitat = fish.habitat,
        maxWeight = fish.maxWeight,
        diet = fish.diet
    )
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) { 
        Box { 
            AsyncImage(
                model = fishProduct.imageUrl, 
                contentDescription = null, 
                contentScale = ContentScale.Crop, 
                modifier = Modifier.fillMaxWidth().height(300.dp), 
                placeholder = painterResource(R.drawable.bg_dolphin),
                error = painterResource(R.drawable.bg_dolphin)
            )
            IconButton(
                onClick = { navController.popBackStack() }, 
                modifier = Modifier.padding(16.dp).background(Color.White.copy(alpha = 0.5f), CircleShape)
            ) { 
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black) 
            } 
        }
        
        Column(modifier = Modifier.padding(24.dp)) { 
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween
            ) { 
                Text(
                    fishProduct.name, 
                    fontSize = 28.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.primary
                )
                var favoriteState by remember { mutableStateOf(isFavorite) }
                LaunchedEffect(isFavorite) {
                    favoriteState = isFavorite
                }
                val scale by animateFloatAsState(
                    targetValue = if (favoriteState) 1.2f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "favoriteScale"
                )
                val alpha by animateFloatAsState(
                    targetValue = if (favoriteState) 1f else 0.7f,
                    animationSpec = tween(durationMillis = 200),
                    label = "favoriteAlpha"
                )
                Icon(
                    imageVector = if (favoriteState) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder, 
                    contentDescription = "Like", 
                    tint = if (favoriteState) Color.Red else Color.Gray, 
                    modifier = Modifier
                        .size(32.dp)
                        .scale(scale)
                        .alpha(alpha)
                        .clickable { 
                            favoriteState = !favoriteState
                            favoriteViewModel.toggleFavorite(fishId)
                            Toast.makeText(
                                context, 
                                if (favoriteState) "Đã thêm vào yêu thích ❤️" else "Đã xóa khỏi yêu thích",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                )
            }
            
            Text(
                fishProduct.price, 
                fontSize = 24.sp, 
                fontWeight = FontWeight.Bold, 
                color = Color.Red
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                fishProduct.category, 
                fontSize = 16.sp, 
                color = Color.Gray, 
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
            
            DetailItem("🌍 Sống ở đâu", fishProduct.habitat)
            DetailItem("⚖️ Cân nặng tối đa", fishProduct.maxWeight)
            DetailItem("🍣 Thích ăn gì", fishProduct.diet)
            
            Spacer(modifier = Modifier.height(40.dp))
            var isAddingToCart by remember { mutableStateOf(false) }
            val buttonScale by animateFloatAsState(
                targetValue = if (isAddingToCart) 0.95f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                ),
                label = "buttonScale"
            )
            val buttonColor by animateColorAsState(
                targetValue = if (isAddingToCart) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary,
                animationSpec = tween(durationMillis = 200),
                label = "buttonColor"
            )
            Button(
                onClick = { 
                    isAddingToCart = true
                    cartViewModel.addToCart(fish)
                    Toast.makeText(context, "Đã thêm vào giỏ hàng 🛒", Toast.LENGTH_SHORT).show()
                    coroutineScope.launch {
                        delay(200)
                        isAddingToCart = false
                    }
                }, 
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .scale(buttonScale), 
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) { 
                if (isAddingToCart) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Thêm vào giỏ hàng", fontWeight = FontWeight.Bold) 
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun CategoryFilterRow(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { option ->
            val isSelected = selectedOption == option
            FilterChip(
                selected = isSelected,
                onClick = { onOptionSelected(option) },
                label = { Text(if (option == "all") "Tất cả" else option) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
        }
    }
}