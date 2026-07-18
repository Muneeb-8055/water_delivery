with open("app/src/main/java/com/pourify/distribution/ui/screens/CustomerDetailScreen.kt", "r") as f:
    content = f.read()

missing_code = """                Row(
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
                ) {"""

target = """                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {"""

content = content.replace(target, missing_code)

with open("app/src/main/java/com/pourify/distribution/ui/screens/CustomerDetailScreen.kt", "w") as f:
    f.write(content)
