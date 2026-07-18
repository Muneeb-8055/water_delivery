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
