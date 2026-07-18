with open("app/src/main/java/com/pourify/distribution/ui/screens/TransactionProcessingScreen.kt", "r") as f:
    content = f.read()

imports = """import androidx.compose.ui.platform.LocalContext
import com.pourify.distribution.utils.PdfHelper
import com.pourify.distribution.utils.WhatsAppDispatcher
import com.pourify.distribution.data.DeliveryChallanEntity
"""
content = content.replace("import com.pourify.distribution.data.TransactionEntity", "import com.pourify.distribution.data.TransactionEntity\n" + imports)

context_code = "val item = inventory.firstOrNull() // Simplify to first item\n    val context = LocalContext.current"
content = content.replace("val item = inventory.firstOrNull() // Simplify to first item", context_code)

old_click = """onClick = { 
                        // Simplified submission
                        if (customer != null && item != null) {
                            val transaction = TransactionEntity(
                                localUuid = UUID.randomUUID().toString(),
                                customerId = customer.customerId,
                                recordClassification = "SALE",
                                amountCharged = quantity * customer.cachedHistoricalRate,
                                amountCollected = quantity * customer.cachedHistoricalRate,
                                itemUnitsDelivered = quantity,
                                packageAssetsRecovered = quantity, // Assume 1-to-1 swap for simplicity
                                epochTimestamp = System.currentTimeMillis() / 1000,
                                syncState = "PENDING"
                            )
                            viewModel.logTransaction(transaction)
                            viewModel.updateVisitStatus(customer.customerId, "DELIVERED")
                            navController.popBackStack()
                        }
                    },"""

new_click = """onClick = { 
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
                    },"""
content = content.replace(old_click, new_click)

with open("app/src/main/java/com/pourify/distribution/ui/screens/TransactionProcessingScreen.kt", "w") as f:
    f.write(content)
