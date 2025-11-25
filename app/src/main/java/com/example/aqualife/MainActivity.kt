package com.example.aqualife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.aqualife.ui.screen.*
import com.example.aqualife.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = hiltViewModel()
                    val currentUser by authViewModel.currentUser.collectAsState()
                    val sessionState by authViewModel.sessionState.collectAsState()

                    LaunchedEffect(currentUser, sessionState) {
                        val isLoggedIn = currentUser != null || sessionState.isLoggedIn
                        val currentRoute = navController.currentDestination?.route
                        if (isLoggedIn && currentRoute != "home") {
                            navController.navigate("home") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else if (!isLoggedIn && currentRoute == "home") {
                            navController.navigate("welcome") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // Handle back button press safely
                    BackHandler(enabled = currentRoute != "welcome" && currentRoute != "home") {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            // If no back stack, navigate to home
                            if (currentRoute != "home") {
                                navController.navigate("home") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                finish() // Exit app if already at home
                            }
                        }
                    }

                    NavHost(navController = navController, startDestination = "welcome") {
                        composable("welcome") { WelcomeScreen(onNavigateToLogin = { navController.navigate("login") }, onNavigateToRegister = { navController.navigate("register") }) }
                        composable("login") { LoginScreen(navController) }
                        composable("register") { RegisterScreen(navController) }
                        composable("otp") { OTPScreen(navController) }
                        composable("facebook_login") { FacebookLoginScreen(navController) }
                        composable("google_login") { GoogleLoginScreen(navController) }

                        // Main App
                        composable("home") { MainScreen(navController) }

                        // CẬP NHẬT: Thêm tham số category để biết user muốn xem loại cá nào
                        // Nếu category = "all" thì hiện hết, nếu = "Cá biển" thì lọc ra cá biển
                        composable(
                            "fish_list/{category}",
                            arguments = listOf(navArgument("category") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val category = backStackEntry.arguments?.getString("category") ?: "all"
                            FishListScreen(navController, category)
                        }

                        composable("favorites") { FavoritesScreen(navController) }

                        composable(
                            "fish_detail/{fishId}",
                            arguments = listOf(navArgument("fishId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val fishId = backStackEntry.arguments?.getString("fishId") ?: "sea_01"
                            FishDetailScreen(navController, fishId)
                        }
                        composable(
                            "post_detail/{postId}",
                            arguments = listOf(navArgument("postId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val postId = backStackEntry.arguments?.getInt("postId") ?: 0
                            PostDetailScreen(navController, postId)
                        }
                        
                        composable("payment") {
                            PaymentScreen(navController)
                        }
                    }
                }
            }
        }
    }
}