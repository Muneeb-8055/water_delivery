package com.pourify.distribution.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.pourify.distribution.data.CustomerEntity
import com.pourify.distribution.ui.MainViewModel
import com.pourify.distribution.ui.components.PourifyTopAppBar
import com.pourify.distribution.ui.theme.Typography
import java.util.UUID

@Composable
fun ManageCustomersScreen(viewModel: MainViewModel, navController: NavController) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PourifyTopAppBar(
                title = "Manage Customers",
                onNavigationClick = { navController.popBackStack() },
                showBackButton = true,
                onSyncClick = {}
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(customers) { customer ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(customer.businessName, style = Typography.titleMedium)
                        Text(customer.contactPhone, style = Typography.bodyMedium)
                        Text("Lat: ${customer.geoLatitude}, Lng: ${customer.geoLongitude}", style = Typography.labelSmall)
                    }
                }
            }
        }
    }

    if (showDialog) {
        val context = LocalContext.current
        val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
        
        var businessName by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var balance by remember { mutableStateOf("") }
        var lat by remember { mutableDoubleStateOf(0.0) }
        var lng by remember { mutableDoubleStateOf(0.0) }
        var locationText by remember { mutableStateOf("No location set") }

        val requestPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            lat = location.latitude
                            lng = location.longitude
                            locationText = "Lat: $lat, Lng: $lng"
                        }
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Register Customer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Business Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = balance,
                        onValueChange = { balance = it },
                        label = { Text("Starting Balance") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(locationText, style = Typography.labelSmall, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                try {
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                        if (location != null) {
                                            lat = location.latitude
                                            lng = location.longitude
                                            locationText = "Lat: $lat, Lng: $lng"
                                        }
                                    }
                                } catch (e: SecurityException) { e.printStackTrace() }
                            } else {
                                requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            }
                        }) {
                            Icon(Icons.Filled.PinDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Tag")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val bal = balance.toDoubleOrNull() ?: 0.0
                    if (businessName.isNotBlank()) {
                        viewModel.insertCustomer(
                            CustomerEntity(
                                customerId = UUID.randomUUID().toString(),
                                businessName = businessName,
                                contactPhone = phone,
                                geoLatitude = lat,
                                geoLongitude = lng,
                                cachedHistoricalRate = 0.0,
                                balanceReceivable = bal,
                                companyOwnedBottles = 0,
                                depositBackedBottles = 0,
                                visitStatus = "SCHEDULED"
                            )
                        )
                        showDialog = false
                    }
                }) { Text("Register") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}
