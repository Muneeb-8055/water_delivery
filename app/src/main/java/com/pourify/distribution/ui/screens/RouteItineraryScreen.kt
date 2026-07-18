package com.pourify.distribution.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pourify.distribution.data.CustomerEntity
import com.pourify.distribution.ui.MainViewModel
import com.pourify.distribution.ui.components.PourifyBottomNavBar
import com.pourify.distribution.ui.components.PourifyTopAppBar
import com.pourify.distribution.ui.theme.Typography

@Composable
fun RouteItineraryScreen(viewModel: MainViewModel, navController: NavController) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            PourifyTopAppBar(
                title = "Pourify",
                onSyncClick = { /*TODO*/ }
            )
        },
        bottomBar = {
            PourifyBottomNavBar(
                currentRoute = "itinerary",
                onNavigateToDashboard = { navController.navigate("dashboard") },
                onNavigateToItinerary = { },
                onNavigateToSync = { },
                onNavigateToSettings = { navController.navigate("profile") } // Let's map settings to profile for now based on UI designs
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Summary Dashboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.inverseSurface, RoundedCornerShape(24.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("REFILLS", style = Typography.labelMedium, color = MaterialTheme.colorScheme.inverseOnSurface)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("12", style = Typography.titleLarge.copy(color = MaterialTheme.colorScheme.inversePrimary))
                        Text(" / 40", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("CASH", style = Typography.labelMedium, color = MaterialTheme.colorScheme.inverseOnSurface)
                    Text("PKR 14,200", style = Typography.titleLarge.copy(color = MaterialTheme.colorScheme.inversePrimary))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs
            Row(modifier = Modifier.fillMaxWidth()) {
                TabButton(text = "All Routes", isSelected = selectedTab == 0, onClick = { selectedTab = 0 }, modifier = Modifier.weight(1f))
                TabButton(text = "Remaining", isSelected = selectedTab == 1, onClick = { selectedTab = 1 }, modifier = Modifier.weight(1f))
                TabButton(text = "Visited", isSelected = selectedTab == 2, onClick = { selectedTab = 2 }, modifier = Modifier.weight(1f))
            }
            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(16.dp))

            val filteredCustomers = when (selectedTab) {
                1 -> customers.filter { it.visitStatus == "SCHEDULED" }
                2 -> customers.filter { it.visitStatus == "DELIVERED" || it.visitStatus == "SKIPPED" }
                else -> customers
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredCustomers) { customer ->
                    RouteItem(customer) {
                        navController.navigate("customer_dashboard/${customer.customerId}")
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = Typography.labelMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .align(Alignment.BottomCenter)
            ) {
                Divider(
                    color = MaterialTheme.colorScheme.primary,
                    thickness = 2.dp,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun RouteItem(customer: CustomerEntity, onClick: () -> Unit) {
    val indicatorColor = when (customer.visitStatus) {
        "DELIVERED" -> Color(0xFF206D3E) // Emerald
        "SKIPPED" -> Color(0xFFF59E0B) // Amber
        else -> Color(0xFF64748B) // Slate
    }
    
    val opacity = if (customer.visitStatus == "SKIPPED") 0.75f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(indicatorColor)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.businessName,
                        style = Typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = opacity)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = customer.contactPhone, // Replace with address if it was available, using phone as proxy
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = opacity)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("BALANCE", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = opacity))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Balance: PKR ${customer.balanceReceivable.toInt()}",
                        style = Typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = opacity)
                    )
                }
            }
        }
    }
}
