package com.pourify.distribution.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pourify.distribution.data.AppRepository
import com.pourify.distribution.data.CustomerEntity
import com.pourify.distribution.data.InventoryItemEntity
import com.pourify.distribution.data.TransactionEntity
import com.pourify.distribution.data.DeliveryChallanEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    val customers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventory: StateFlow<List<InventoryItemEntity>> = repository.allInventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Seed initial data if empty
            if (customers.value.isEmpty()) {
                
                val initialInventory = listOf(
                    InventoryItemEntity("WR-19L", "19L Bottle Refill", "FINISHED_GOOD", 4.50, 150, 112, 38)
                )
                repository.insertInventory(initialInventory)

                val dummyCustomers = listOf(
                    CustomerEntity("cust-1", "Shuaib Corp", "0300-1234567", 31.5204, 74.3587, 120.0, 480.0, 6, 2, "SCHEDULED"),
                    CustomerEntity("cust-2", "Ali Traders", "0300-7654321", 31.5210, 74.3590, 110.0, 0.0, 4, 0, "SCHEDULED"),
                    CustomerEntity("cust-3", "Zaid Mart", "0300-9998887", 31.5220, 74.3600, 130.0, 1500.0, 10, 5, "SCHEDULED"),
                )
                repository.insertCustomers(dummyCustomers)

            }
        }
    }

    
    fun insertCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.insertCustomers(listOf(customer))
        }
    }

    fun updateCustomerLocation(customerId: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.getCustomerById(customerId)?.let {
                repository.updateCustomer(it.copy(geoLatitude = lat, geoLongitude = lng))
            }
        }
    }

    fun updateVisitStatus(customerId: String, status: String) {
        viewModelScope.launch {
            repository.getCustomerById(customerId)?.let {
                repository.updateCustomer(it.copy(visitStatus = status))
            }
        }
    }

    
    fun insertChallan(challan: DeliveryChallanEntity) {
        viewModelScope.launch {
            repository.insertChallan(challan)
        }
    }

    suspend fun getUnpaidChallans(customerId: String): List<DeliveryChallanEntity> {
        return repository.getUnpaidChallans(customerId)
    }

    fun logTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }
}

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
