package com.pourify.distribution.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pourify.distribution.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PourifyTopAppBar(
    title: String = "Pourify",
    onNavigationClick: (() -> Unit)? = null,
    onSyncClick: () -> Unit,
    showBackButton: Boolean = false
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = Typography.displayMedium.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary
            )
        },
        navigationIcon = {
            if (showBackButton && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Filled.SignalCellular4Bar,
                        contentDescription = "Network Status",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onSyncClick) {
                Icon(
                    imageVector = Icons.Filled.Sync,
                    contentDescription = "Sync",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun PourifyBottomNavBar(
    currentRoute: String,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToItinerary: () -> Unit,
    onNavigateToSync: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "dashboard",
            onClick = onNavigateToDashboard,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Dashboard, 
                    contentDescription = "Dashboard"
                )
            },
            label = { Text("Dashboard", style = Typography.labelMedium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        NavigationBarItem(
            selected = currentRoute == "itinerary",
            onClick = onNavigateToItinerary,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Route, 
                    contentDescription = "Itinerary"
                )
            },
            label = { Text("Itinerary", style = Typography.labelMedium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        NavigationBarItem(
            selected = currentRoute == "sync",
            onClick = onNavigateToSync,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Sync, 
                    contentDescription = "Sync"
                )
            },
            label = { Text("Sync", style = Typography.labelMedium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        NavigationBarItem(
            selected = currentRoute == "settings",
            onClick = onNavigateToSettings,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Settings, 
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings", style = Typography.labelMedium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
