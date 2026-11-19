package com.example.cooperativadeliveryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cooperativadeliveryapp.domain.model.UserRole
import com.example.cooperativadeliveryapp.di.LocalFirebaseSeeder
import com.example.cooperativadeliveryapp.presentation.auth.AuthNavHost
import com.example.cooperativadeliveryapp.presentation.business.AddEditProductScreen
import com.example.cooperativadeliveryapp.presentation.business.BusinessDashboardScreen
import com.example.cooperativadeliveryapp.presentation.client.BusinessDetailScreen
import com.example.cooperativadeliveryapp.presentation.client.CartCheckoutScreen
import com.example.cooperativadeliveryapp.presentation.client.ClientHomeScreen
import com.example.cooperativadeliveryapp.presentation.courier.CourierActiveDeliveryScreen
import com.example.cooperativadeliveryapp.presentation.courier.CourierDashboardScreen
import com.example.cooperativadeliveryapp.presentation.courier.CourierRoutePreviewScreen
import com.example.cooperativadeliveryapp.presentation.login.LoginScreen
import com.example.cooperativadeliveryapp.ui.theme.CooperativaDeliveryAppTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.cooperativadeliveryapp.BuildConfig

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var seeder: LocalFirebaseSeeder
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            if (BuildConfig.DEBUG) {
                seeder.seedDatabaseIfEmpty()
            }
        }
        
        enableEdgeToEdge()
        setContent {
            CooperativaDeliveryAppTheme {
                val navController = rememberNavController()
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController = navController, startDestination = "login") {
                            composable("login") {
                                LoginScreen(
                                    onNavigateToDashboard = { role ->
                                        when (role) {
                                            UserRole.BUSINESS -> {
                                                navController.navigate("business_dashboard") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                            UserRole.CLIENT -> {
                                                navController.navigate("client_home") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                            UserRole.COURIER -> {
                                                navController.navigate("courier_dashboard") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                        }
                                    },
                                    onNavigateToRegister = {
                                        navController.navigate("auth")
                                    }
                                )
                            }
                            
                            composable("auth") {
                                AuthNavHost(
                                    onAuthSuccess = { role ->
                                        when (role) {
                                            UserRole.BUSINESS -> {
                                                navController.navigate("business_dashboard") {
                                                    popUpTo("auth") { inclusive = true }
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                            UserRole.CLIENT -> {
                                                navController.navigate("client_home") {
                                                    popUpTo("auth") { inclusive = true }
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                            UserRole.COURIER -> {
                                                navController.navigate("courier_dashboard") {
                                                    popUpTo("auth") { inclusive = true }
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                        }
                                    },
                                    onNavigateToLogin = {
                                        navController.popBackStack("login", inclusive = false)
                                    }
                                )
                            }
                            
                            composable("business_dashboard") {
                                BusinessDashboardScreen(
                                    onNavigateToEditProduct = { productId ->
                                        if (productId == null) {
                                            navController.navigate("add_edit_product")
                                        } else {
                                            navController.navigate("add_edit_product?productId=$productId")
                                        }
                                    },
                                    onLogout = {
                                        navController.navigate("login") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            
                            composable(
                                route = "add_edit_product?productId={productId}",
                                arguments = listOf(navArgument("productId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                })
                            ) {
                                AddEditProductScreen(
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                            
                            composable("client_home") {
                                ClientHomeScreen(
                                    onNavigateToBusiness = { businessId ->
                                        navController.navigate("business_detail/$businessId")
                                    },
                                    onNavigateToCart = {
                                        navController.navigate("cart_checkout")
                                    },
                                    onLogout = {
                                        navController.navigate("login") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            
                            composable(
                                route = "business_detail/{businessId}",
                                arguments = listOf(navArgument("businessId") { type = NavType.StringType })
                            ) {
                                BusinessDetailScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToCart = { navController.navigate("cart_checkout") }
                                )
                            }
                            
                            composable("cart_checkout") {
                                CartCheckoutScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onOrderSuccess = {
                                        navController.navigate("client_home") {
                                            popUpTo("client_home") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("courier_dashboard") {
                                CourierDashboardScreen(
                                    onNavigateToRoutePreview = { orderId ->
                                        navController.navigate("courier_route_preview/$orderId")
                                    },
                                    onLogout = {
                                        navController.navigate("login") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            
                            composable(
                                route = "courier_route_preview/{orderId}",
                                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                            ) {
                                CourierRoutePreviewScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onOrderAccepted = {
                                        val orderId = it.arguments?.getString("orderId")
                                        if (orderId != null) {
                                            navController.navigate("courier_active_delivery/$orderId") {
                                                popUpTo("courier_dashboard") { inclusive = false }
                                            }
                                        }
                                    }
                                )
                            }
                            
                            composable(
                                route = "courier_active_delivery/{orderId}",
                                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                            ) {
                                CourierActiveDeliveryScreen(
                                    onDeliveryFinished = {
                                        navController.popBackStack("courier_dashboard", inclusive = false)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

