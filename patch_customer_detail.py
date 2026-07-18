with open("app/src/main/java/com/pourify/distribution/ui/screens/CustomerDetailScreen.kt", "r") as f:
    content = f.read()

import_delivery = "import com.pourify.distribution.data.DeliveryChallanEntity"
if import_delivery not in content:
    content = content.replace("import com.pourify.distribution.utils.PdfHelper", import_delivery + "\nimport com.pourify.distribution.utils.PdfHelper")

import_scroll = "import androidx.compose.foundation.verticalScroll\nimport androidx.compose.foundation.rememberScrollState"
if "rememberScrollState" not in content:
    content = content.replace("import androidx.compose.foundation.layout.*", "import androidx.compose.foundation.layout.*\n" + import_scroll)

state_unpaid = """
    val unpaidChallans by produceState<List<DeliveryChallanEntity>>(initialValue = emptyList(), customerId) {
        value = viewModel.getUnpaidChallans(customerId)
    }
"""

if "produceState<List<DeliveryChallanEntity>>" not in content:
    content = content.replace("val requestPermissionLauncher =", state_unpaid + "\n    val requestPermissionLauncher =")

scroll_mod = "                    .padding(16.dp)\n                    .verticalScroll(rememberScrollState())"
if "verticalScroll" not in content:
    content = content.replace("                    .padding(16.dp)", scroll_mod, 1)

challans_ui = """
                Spacer(modifier = Modifier.height(24.dp))
                Text("UNPAID CHALLANS", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (unpaidChallans.isEmpty()) {
                    Text("No unpaid challans.", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    unpaidChallans.forEach { challan ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Challan: ${challan.id}", style = Typography.labelLarge)
                                }
                                Icon(imageVector = Icons.Filled.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
"""

if "UNPAID CHALLANS" not in content:
    content = content.replace('Spacer(modifier = Modifier.height(24.dp))\n                Text("ACTIONS"', challans_ui + '\n                Spacer(modifier = Modifier.height(24.dp))\n                Text("ACTIONS"')

with open("app/src/main/java/com/pourify/distribution/ui/screens/CustomerDetailScreen.kt", "w") as f:
    f.write(content)
