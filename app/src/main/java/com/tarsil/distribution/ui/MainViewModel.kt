package com.tarsil.distribution.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tarsil.distribution.data.AppRepository
import com.tarsil.distribution.data.CustomerEntity
import com.tarsil.distribution.data.InventoryItemEntity
import com.tarsil.distribution.data.TransactionEntity
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
                val initialCustomers = listOf(
                    CustomerEntity("8849", "Acme Corp", "+923001234567", 31.5204, 74.3587, 4.50, 0.0, 5, 0, "SCHEDULED"),
                    CustomerEntity("8850", "Shuaib Corp", "+923001234568", 31.5205, 74.3588, 120.0, 2400.0, 6, 0, "SCHEDULED"),
                    CustomerEntity("8851", "Al-Fatah Stores", "+923001234569", 31.5206, 74.3589, 150.0, 4500.0, 10, 0, "DELIVERED"),
                    CustomerEntity("8852", "Rana Supermart", "+923001234570", 31.5207, 74.3590, 150.0, 0.0, 0, 0, "SKIPPED"),
                    CustomerEntity("8853", "Imtiaz Super Market", "+923001234571", 31.5208, 74.3591, 150.0, 12000.0, 20, 0, "SCHEDULED"),
                    CustomerEntity("8854", "Makkah General Store", "+923001234572", 31.5209, 74.3592, 150.0, 1250.0, 2, 0, "SCHEDULED")
                )
                repository.insertCustomers(initialCustomers)
                
                val initialInventory = listOf(
                    InventoryItemEntity("WR-19L", "19L Bottle Refill", "FINISHED_GOOD", 4.50, 150, 112, 38)
                )
                repository.insertInventory(initialInventory)
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
