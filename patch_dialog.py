with open("app/src/main/java/com/pourify/distribution/ui/screens/RouteItineraryScreen.kt", "r") as f:
    lines = f.readlines()

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

lines.insert(126, dialog_code)

with open("app/src/main/java/com/pourify/distribution/ui/screens/RouteItineraryScreen.kt", "w") as f:
    f.writelines(lines)
    print("Patched RouteItineraryScreen with Dialog")
