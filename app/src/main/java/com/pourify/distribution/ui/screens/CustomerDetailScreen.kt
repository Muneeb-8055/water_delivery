package com.pourify.distribution.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import androidx.compose.material.icons.filled.PinDrop
import com.pourify.distribution.utils.WhatsAppDispatcher
import com.pourify.distribution.utils.PdfHelper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pourify.distribution.ui.MainViewModel
import com.pourify.distribution.ui.components.PourifyBottomNavBar
import com.pourify.distribution.ui.components.PourifyTopAppBar
import com.pourify.distribution.ui.theme.Typography

@Composable
fun CustomerDetailScreen(
    customerId: String,
    viewModel: MainViewModel,
    navController: NavController
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val customer = customers.find { it.customerId == customerId }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationText by remember { mutableStateOf("Lat: ${customer?.geoLatitude ?: 0.0}, Lng: ${customer?.geoLongitude ?: 0.0}") }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.updateCustomerLocation(customerId, location.latitude, location.longitude)
                        locationText = "Lat: ${location.latitude}, Lng: ${location.longitude}"
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            PourifyTopAppBar(
                title = "FieldOps Pro",
                onNavigationClick = { navController.popBackStack() },
                onSyncClick = { },
                showBackButton = true
            )
        },
        bottomBar = {
            PourifyBottomNavBar(
                currentRoute = "itinerary",
                onNavigateToItinerary = { navController.navigate("itinerary") },
                onNavigateToSync = { },
                onNavigateToSettings = { navController.navigate("profile") }
            )
        }
    ) { innerPadding ->
        if (customer != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(Color(0xFF206D3E))
                        )
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(text = customer.businessName, style = Typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = customer.contactPhone, style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text(
                                        text = "PRIORITY",
                                        style = Typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Map placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(96.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = locationText, style = Typography.bodySmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedButton(
                                        onClick = {
                                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                                try {
                                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                                        if (location != null) {
                                                            viewModel.updateCustomerLocation(customerId, location.latitude, location.longitude)
                                                            locationText = "Lat: ${location.latitude}, Lng: ${location.longitude}"
                                                        }
                                                    }
                                                } catch (e: SecurityException) {
                                                    e.printStackTrace()
                                                }
                                            } else {
                                                requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                            }
                                        },
                                        modifier = Modifier.height(36.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Filled.PinDrop, contentDescription = "Pin Location", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pin Location", style = Typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Financial & Asset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Outstanding
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(128.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(Color(0xFFF59E0B)))
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("OUTSTANDING", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("PKR ${customer.balanceReceivable.toInt()}", style = Typography.headlineSmall, color = MaterialTheme.colorScheme.error)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("View Details", style = Typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                    Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    
                    // Empties
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(128.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(Color(0xFF64748B)))
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("EMPTIES HELD", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${customer.companyOwnedBottles} Units", style = Typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Audit", style = Typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.Filled.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("ACTIONS", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Primary Action
                Button(
                    onClick = { navController.navigate("transaction/$customerId") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Log Delivery", style = Typography.headlineSmall)
                        }
                        Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = null)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Secondary Action
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Payments, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Log Payment Recovery", style = Typography.headlineSmall)
                        }
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val unpaid = viewModel.getUnpaidChallans(customerId)
                                val invoiceFile = PdfHelper.generateConsolidatedInvoice(context, customer, unpaid)
                                if (invoiceFile != null) {
                                    WhatsAppDispatcher.sendPdfViaWhatsApp(
                                        context,
                                        customer.contactPhone,
                                        invoiceFile,
                                        "Hello, here is your consolidated invoice from Pourify. Thank you!"
                                    )
                                } else {
                                    WhatsAppDispatcher.sendInvoiceText(context, customer.contactPhone, customer.balanceReceivable)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF206D3E)),
                        border = BorderStroke(1.dp, Color(0xFF206D3E))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Filled.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate & Share Invoice", style = Typography.labelMedium)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { 
                        viewModel.updateVisitStatus(customerId, "SKIPPED")
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Filled.Block, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Skip (No Response)", style = Typography.bodyLarge)
                    }
                }
            }
        } else {
            // Customer not found
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Customer not found")
            }
        }
    }
}
