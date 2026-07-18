import re

with open("app/src/main/java/com/pourify/distribution/ui/screens/CustomerDetailScreen.kt", "r") as f:
    content = f.read()

# Add rememberCoroutineScope import
if "import kotlinx.coroutines.launch" not in content:
    content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport kotlinx.coroutines.launch")

if "import com.pourify.distribution.utils.PdfHelper" not in content:
    content = content.replace("import com.pourify.distribution.utils.WhatsAppDispatcher", "import com.pourify.distribution.utils.WhatsAppDispatcher\nimport com.pourify.distribution.utils.PdfHelper")

# Add scope
content = content.replace("val context = LocalContext.current", "val context = LocalContext.current\n    val scope = rememberCoroutineScope()")

target = """                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {"""
end_target = """                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { 
                        viewModel.updateVisitStatus(customerId, "SKIPPED")"""

# Extract the block to replace
start_idx = content.find(target)
end_idx = content.find(end_target)

replacement = """                Row(
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
                
"""
content = content[:start_idx] + replacement + content[end_idx:]

with open("app/src/main/java/com/pourify/distribution/ui/screens/CustomerDetailScreen.kt", "w") as f:
    f.write(content)
