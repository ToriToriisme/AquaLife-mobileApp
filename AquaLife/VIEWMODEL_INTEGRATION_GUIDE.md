# ViewModel Integration Guide

## How to Update Existing Screens to Use ViewModels

### Example: Updating HomeScreenContent

**Before (Using Global State):**
```kotlin
@Composable
fun HomeScreenContent(navController: NavController) {
    val fishList = largeFishList // Global variable
    // ...
}
```

**After (Using ViewModel):**
```kotlin
@Composable
fun HomeScreenContent(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val fishList by viewModel.allFish.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    if (isLoading) {
        SkeletonGrid() // Show skeleton while loading
    } else {
        LazyColumn {
            items(fishList) { fish ->
                FishCard(fish)
            }
        }
    }
}
```

### Example: Updating CartScreen

**Before:**
```kotlin
@Composable
fun CartScreen(navController: NavController) {
    val cartItems = globalCartItems // Global variable
    // ...
}
```

**After:**
```kotlin
@Composable
fun CartScreen(
    navController: NavController,
    viewModel: CartViewModel = hiltViewModel()
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    
    // Use cartItems and totalPrice from ViewModel
}
```

### Example: Updating Auth Screens

**Before:**
```kotlin
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    // Manual login logic
}
```

**After:**
```kotlin
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.authError.collectAsState()
    
    // Observe auth state
    LaunchedEffect(Unit) {
        viewModel.currentUser.collect { user ->
            if (user != null) {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
    }
    
    Button(onClick = { viewModel.login(email, password) }) {
        if (isLoading) CircularProgressIndicator() else Text("Login")
    }
    
    error?.let { Text(it, color = Color.Red) }
}
```

## Key Points

1. **Always use `hiltViewModel()`** to get ViewModel instance
2. **Collect StateFlow as State** using `collectAsState()`
3. **Observe navigation** using `LaunchedEffect` when auth state changes
4. **Show loading states** using `isLoading` from ViewModel
5. **Handle errors** using `error` StateFlow from ViewModel

## Navigation Updates

Add payment route to MainActivity:
```kotlin
composable("payment") { 
    PaymentScreen(navController) 
}
```

Update CartScreen checkout button:
```kotlin
Button(onClick = { navController.navigate("payment") }) {
    Text("Thanh Toán")
}
```

