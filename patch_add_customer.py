with open("app/src/main/java/com/pourify/distribution/ui/screens/RouteItineraryScreen.kt", "r") as f:
    content = f.read()

import_add = "import androidx.compose.material.icons.filled.Add"
if import_add not in content:
    content = content.replace("import androidx.compose.material.icons.Icons", "import androidx.compose.material.icons.Icons\n" + import_add)
if "import java.util.UUID" not in content:
    content = content.replace("package com.pourify.distribution.ui.screens", "package com.pourify.distribution.ui.screens\n\nimport java.util.UUID\n")

if "var showAddCustomerDialog" not in content:
    content = content.replace("var selectedTab by remember { mutableStateOf(0) }", "var selectedTab by remember { mutableStateOf(0) }\n    var showAddCustomerDialog by remember { mutableStateOf(false) }")
    
    fab_code = """
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCustomerDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Customer")
            }
        },"""
    content = content.replace("        bottomBar = {", fab_code + "\n        bottomBar = {")
    
    dialog_code = """
    if (showAddCustomerDialog) {
        var businessName by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var balance by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAddCustomerDialog = false },
            title = { Text("Add New Customer") },
            text = {
                Column {
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Business Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = balance,
                        onValueChange = { balance = it },
                        label = { Text("Starting Balance") },
                        modifier = Modifier.fillMaxWidth()
                    )
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
                                geoLatitude = 0.0,
                                geoLongitude = 0.0,
                                balanceReceivable = bal,
                                companyOwnedBottles = 0,
                                visitStatus = "SCHEDULED",
                                tenantId = "b1234567-89ab-cdef-0123-456789abcdef",
                                syncStatus = 0
                            )
                        )
                        showAddCustomerDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
"""
    content = content.replace("    } // end Scaffold", dialog_code + "\n    }")
    
    with open("app/src/main/java/com/pourify/distribution/ui/screens/RouteItineraryScreen.kt", "w") as f:
        f.write(content)
        print("Patched RouteItineraryScreen")
