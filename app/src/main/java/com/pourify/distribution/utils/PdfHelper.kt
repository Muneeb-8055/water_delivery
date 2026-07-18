package com.pourify.distribution.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.pourify.distribution.data.CustomerEntity
import com.pourify.distribution.data.DeliveryChallanEntity
import com.pourify.distribution.data.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfHelper {
    fun generateDeliveryChallan(
        context: Context,
        customer: CustomerEntity,
        transaction: TransactionEntity,
        challanId: String
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(400, 500, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
        }

        canvas.drawText("Pourify - Delivery Challan", 20f, 40f, paint)
        paint.textSize = 12f
        canvas.drawText("Challan ID: $challanId", 20f, 70f, paint)
        canvas.drawText("Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", 20f, 90f, paint)
        canvas.drawText("Customer: ${customer.businessName}", 20f, 110f, paint)
        
        canvas.drawText("Items Delivered: ${transaction.itemUnitsDelivered}", 20f, 150f, paint)
        canvas.drawText("Empties Recovered: ${transaction.packageAssetsRecovered}", 20f, 170f, paint)
        canvas.drawText("Amount Charged: PKR ${transaction.amountCharged}", 20f, 190f, paint)
        canvas.drawText("Amount Collected: PKR ${transaction.amountCollected}", 20f, 210f, paint)

        pdfDocument.finishPage(page)

        val dir = File(context.cacheDir, "pdfs")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "Challan_$challanId.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        return file
    }

    fun generateConsolidatedInvoice(
        context: Context,
        customer: CustomerEntity,
        unpaidChallans: List<DeliveryChallanEntity>
    ): File? {
        if (unpaidChallans.isEmpty()) return null
        
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(400, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
        }

        canvas.drawText("Pourify - Consolidated Invoice", 20f, 40f, paint)
        paint.textSize = 12f
        canvas.drawText("Customer: ${customer.businessName}", 20f, 70f, paint)
        canvas.drawText("Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}", 20f, 90f, paint)
        
        var yOffset = 130f
        canvas.drawText("Unpaid Challans:", 20f, yOffset, paint)
        yOffset += 20f

        for (challan in unpaidChallans) {
            canvas.drawText("- Challan ID: ${challan.id}", 30f, yOffset, paint)
            yOffset += 20f
        }
        
        yOffset += 20f
        canvas.drawText("Total Receivable: PKR ${customer.balanceReceivable}", 20f, yOffset, paint)

        pdfDocument.finishPage(page)

        val dir = File(context.cacheDir, "pdfs")
        if (!dir.exists()) dir.mkdirs()
        val invoiceId = System.currentTimeMillis()
        val file = File(dir, "Invoice_$invoiceId.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        return file
    }
}
