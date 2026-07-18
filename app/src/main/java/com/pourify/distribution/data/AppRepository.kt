package com.pourify.distribution.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val customerDao: CustomerDao,
    private val inventoryDao: InventoryDao,
    private val transactionDao: TransactionDao,
    private val deliveryChallanDao: DeliveryChallanDao
) {
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val allInventory: Flow<List<InventoryItemEntity>> = inventoryDao.getAllInventory()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun insertCustomers(customers: List<CustomerEntity>) {
        customerDao.insertAll(customers)
    }
    
    suspend fun deleteAllCustomers() {
        customerDao.deleteAllCustomers()
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        customerDao.updateCustomer(customer)
    }

    suspend fun getCustomerById(id: String): CustomerEntity? {
        return customerDao.getCustomerById(id)
    }

    suspend fun insertInventory(items: List<InventoryItemEntity>) {
        inventoryDao.insertAll(items)
    }

    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }
    
    suspend fun getPendingTransactions(): List<TransactionEntity> {
        return transactionDao.getPendingTransactions()
    }
    
    suspend fun updateTransactions(transactions: List<TransactionEntity>) {
        transactionDao.updateTransactions(transactions)
    }
    
    suspend fun insertChallan(challan: DeliveryChallanEntity) {
        deliveryChallanDao.insertChallan(challan)
    }
    
    suspend fun getUnpaidChallans(customerId: String): List<DeliveryChallanEntity> {
        return deliveryChallanDao.getUnpaidChallansForCustomer(customerId)
    }
}
