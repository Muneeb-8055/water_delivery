package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val customerId: String,
    val businessName: String,
    val contactPhone: String,
    val geoLatitude: Double,
    val geoLongitude: Double,
    val cachedHistoricalRate: Double,
    val balanceReceivable: Double,
    val companyOwnedBottles: Int,
    val depositBackedBottles: Int,
    val visitStatus: String // "SCHEDULED", "DELIVERED", "SKIPPED"
)

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey val itemId: String,
    val title: String,
    val classification: String,
    val baseRate: Double,
    val loadedUnits: Int,
    val soldUnits: Int,
    val remainingUnits: Int
)

@Entity(tableName = "transaction_ledger")
data class TransactionEntity(
    @PrimaryKey val localUuid: String,
    val customerId: String,
    val recordClassification: String, // 'SALE', 'RECOVERY', 'SKIP'
    val amountCharged: Double,
    val amountCollected: Double,
    val itemUnitsDelivered: Int,
    val packageAssetsRecovered: Int,
    val epochTimestamp: Long,
    val syncState: String // 'PENDING', 'SYNCED'
)
