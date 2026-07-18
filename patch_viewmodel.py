import re

with open("app/src/main/java/com/pourify/distribution/ui/MainViewModel.kt", "r") as f:
    content = f.read()

imports = """import com.pourify.distribution.data.DeliveryChallanEntity
"""
content = re.sub(r'import com.pourify.distribution.data.TransactionEntity\n', 'import com.pourify.distribution.data.TransactionEntity\n' + imports, content)

new_methods = """
    fun insertChallan(challan: DeliveryChallanEntity) {
        viewModelScope.launch {
            repository.insertChallan(challan)
        }
    }

    suspend fun getUnpaidChallans(customerId: String): List<DeliveryChallanEntity> {
        return repository.getUnpaidChallans(customerId)
    }
"""

content = content.replace("fun logTransaction(transaction: TransactionEntity) {", new_methods + "\n    fun logTransaction(transaction: TransactionEntity) {")

with open("app/src/main/java/com/pourify/distribution/ui/MainViewModel.kt", "w") as f:
    f.write(content)
