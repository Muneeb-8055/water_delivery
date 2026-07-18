package com.tarsil.distribution

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tarsil.distribution.data.AppDatabase
import com.tarsil.distribution.data.AppRepository
import com.tarsil.distribution.ui.MainViewModel
import com.tarsil.distribution.ui.MainViewModelFactory
import com.tarsil.distribution.ui.screens.*
import com.tarsil.distribution.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val repository = AppRepository(database.customerDao(), database.inventoryDao(), database.transactionDao())
        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setContent {
            MyApplicationTheme(dynamicColor = false) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { navController.navigate("dashboard") { popUpTo("login") { inclusive = true } } }
                        )
                    }
                    composable("dashboard") {
                        UnifiedDashboardScreen(
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                    composable("itinerary") {
                        ItineraryScreen(
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                    composable("customer_dashboard/{customerId}") { backStackEntry ->
                        val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
                        CustomerDashboardScreen(
                            customerId = customerId,
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                    composable("transaction/{customerId}") { backStackEntry ->
                        val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
                        TransactionProcessingScreen(
                            customerId = customerId,
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                    composable("settings") {
                        SettingsScreen(navController = navController)
                    }
                    composable("profile") {
                        DriverProfileScreen(navController = navController)
                    }
                    composable("reconciliation") {
                        ReconciliationScreen(navController = navController)
                    }
                }
            }
        }
    }
}
