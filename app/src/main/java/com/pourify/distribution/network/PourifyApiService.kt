package com.pourify.distribution.network

import com.pourify.distribution.data.CustomerEntity
import com.pourify.distribution.data.TransactionEntity
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class ReconcileRequest(
    val tenant_id: String,
    val driver_id: String,
    val transactions: List<TransactionEntity>,
    val gps_logs: List<Any>
)

data class ReconcileResponse(
    val success: Boolean,
    val reconciled: List<String>?
)

data class ProvisionResponse(
    val success: Boolean,
    val customers: List<CustomerEntity>?
)

interface PourifyApiService {
    @POST("/api/v1/mobile/itinerary/reconcile")
    suspend fun reconcile(@Body request: ReconcileRequest): ReconcileResponse

    @GET("/api/v1/mobile/itinerary/provision")
    suspend fun provision(
        @Query("tenant_id") tenantId: String,
        @Query("driver_id") driverId: String
    ): ProvisionResponse
}
