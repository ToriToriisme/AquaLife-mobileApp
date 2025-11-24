package com.example.aqualife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aqualife.ui.screen.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

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
                            arguments = listOf(navArgument("fishId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val fishId = backStackEntry.arguments?.getInt("fishId") ?: 1
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