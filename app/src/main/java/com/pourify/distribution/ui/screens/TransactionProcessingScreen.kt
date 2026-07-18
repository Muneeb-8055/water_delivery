package com.pourify.distribution.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pourify.distribution.data.TransactionEntity
import androidx.compose.ui.platform.LocalContext
import com.pourify.distribution.utils.PdfHelper
import com.pourify.distribution.utils.WhatsAppDispatcher
import com.pourify.distribution.data.DeliveryChallanEntity

import com.pourify.distribution.ui.MainViewModel
import com.pourify.distribution.ui.components.PourifyTopAppBar
import com.pourify.distribution.ui.theme.Typography
import java.util.UUID

@Composable
fun TransactionProcessingScreen(
    customerId: String,
    viewModel: MainViewModel,
    navController: NavController
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()
    
    val customer = customers.find { it.customerId == customerId }
    val item = inventory.firstOrNull() // Simplify to first item
    val context = LocalContext.current
    
    var quantity by remember { mutableStateOf(5) }

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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { 
                        if (customer != null && item != null) {
                            val transaction = TransactionEntity(
                                localUuid = UUID.randomUUID().toString(),
                                customerId = customer.customerId,
                                recordClassification = "SALE",
                                amountCharged = quantity * customer.cachedHistoricalRate,
                                amountCollected = quantity * customer.cachedHistoricalRate,
                                itemUnitsDelivered = quantity,
                                packageAssetsRecovered = quantity, // Assume 1-to-1 swap for simplicity
                                epochTimestamp = System.currentTimeMillis(),
                                syncState = "PENDING"
                            )
                            viewModel.logTransaction(transaction)
                            viewModel.updateVisitStatus(customer.customerId, "DELIVERED")
                            
                            // 1. Generate PDF Challan
                            val challanId = "CHL-${System.currentTimeMillis()}"
                            val pdfFile = PdfHelper.generateDeliveryChallan(context, customer, transaction, challanId)
                            
                            // 2. Save DeliveryChallanEntity
                            val challanEntity = DeliveryChallanEntity(
                                id = challanId,
                                customerId = customer.customerId,
                                transactionUuid = transaction.localUuid,
                                pdfFilePath = pdfFile.absolutePath,
                                isPaid = false
                            )
                            viewModel.insertChallan(challanEntity)
                            
                            // 3. Trigger WhatsApp
                            WhatsAppDispatcher.sendPdfViaWhatsApp(
                                context,
                                customer.contactPhone,
                                pdfFile,
                                "Hello, here is your delivery challan from Pourify. Thank you!"
                            )
                            
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp)
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CONTINUE TO PAYMENT", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    ) { innerPadding ->
        if (customer != null && item != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text("New Transaction", style = Typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Storefront, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${customer.businessName} (ID: ${customer.customerId})", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StepItem("Product", 1, true)
                    Box(modifier = Modifier.weight(1f).height(2.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                    StepItem("Price", 2, false)
                    Box(modifier = Modifier.weight(1f).height(2.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                    StepItem("Payment", 3, false)
                    Box(modifier = Modifier.weight(1f).height(2.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                    StepItem("Sign", 4, false)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Select Product Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(MaterialTheme.colorScheme.secondaryContainer))
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Select Product", style = Typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Filled.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(item.title, style = Typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("SKU: ${item.itemId}", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                
                                // Quantity Selector
                                Row(
                                    modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { if (quantity > 0) quantity-- }, modifier = Modifier.size(40.dp)) {
                                        Icon(imageVector = Icons.Filled.Remove, contentDescription = null)
                                    }
                                    Text(quantity.toString(), style = Typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                    IconButton(onClick = { quantity++ }, modifier = Modifier.size(40.dp)) {
                                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedButton(
                                onClick = { },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(imageVector = Icons.Filled.AddCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ADD ANOTHER PRODUCT", style = Typography.labelMedium)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Price Verification Card
                Card(
                    modifier = Modifier.fillMaxWidth().alpha(0.6f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant))
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Price Verification", style = Typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text("UNIT PRICE (HISTORICAL RATE)", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = String.format("%.2f", customer.cachedHistoricalRate),
                                onValueChange = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Text("$", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                trailingIcon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("SUBTOTAL ($quantity ITEMS)", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "$${String.format("%.2f", quantity * customer.cachedHistoricalRate)}",
                                    style = Typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepItem(label: String, stepNumber: Int, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (isActive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                style = Typography.labelMedium,
                color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = Typography.labelMedium,
            color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
