package com.pourify.distribution.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pourify.distribution.data.AppDatabase
import com.pourify.distribution.data.AppRepository
import com.pourify.distribution.network.ReconcileRequest
import com.pourify.distribution.network.PourifyApiService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(database.customerDao(), database.inventoryDao(), database.transactionDao(), database.deliveryChallanDao())

        // Ensure you change BASE_URL to your actual backend IP or domain in production
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000") // Assuming localhost emulator access to local backend
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val apiService = retrofit.create(PourifyApiService::class.java)
        
        val tenantId = "b1234567-89ab-cdef-0123-456789abcdef" // Dummy tenant
        val driverId = "d1234567-89ab-cdef-0123-456789abcdef" // Dummy driver

        try {
            // 1. PUSH
            val pendingTransactions = repository.getPendingTransactions()
            val request = ReconcileRequest(
                tenant_id = tenantId,
                driver_id = driverId,
                transactions = pendingTransactions,
                gps_logs = emptyList() // GPS logs not fully implemented yet in DB
            )

            val pushResponse = apiService.reconcile(request)
            if (pushResponse.success) {
                if (pendingTransactions.isNotEmpty()) {
                    val syncedTransactions = pendingTransactions.map { it.copy(syncState = "SYNCED") }
                    repository.updateTransactions(syncedTransactions)
                }
            } else {
                return Result.retry()
            }

            // 2. PULL
            val pullResponse = apiService.provision(tenantId, driverId)
            if (pullResponse.success && pullResponse.customers != null) {
                repository.deleteAllCustomers()
                repository.insertCustomers(pullResponse.customers)
            }
            
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
