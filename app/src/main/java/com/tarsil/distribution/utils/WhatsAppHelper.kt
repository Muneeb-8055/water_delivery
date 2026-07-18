package com.tarsil.distribution.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.tarsil.distribution.data.CustomerEntity
import java.io.File
import java.io.FileOutputStream

object WhatsAppHelper {

    fun sendInvoice(context: Context, customer: CustomerEntity, amount: Double) {
        try {
            // 1. Generate PDF
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(400, 600, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            
            val canvas: Canvas = page.canvas
            val paint = Paint()
            paint.color = Color.BLACK
            
            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText("TARSIL INVOICE", 100f, 60f, paint)
            
            paint.textSize = 16f
            paint.isFakeBoldText = false
            canvas.drawText("Customer: ${customer.businessName}", 40f, 120f, paint)
            canvas.drawText("Phone: ${customer.contactPhone}", 40f, 150f, paint)
            
            canvas.drawText("--------------------------------------------------", 40f, 180f, paint)
            canvas.drawText("Total Amount Due: PKR $amount", 40f, 210f, paint)
            canvas.drawText("Previous Balance: PKR ${customer.balanceReceivable}", 40f, 240f, paint)
            canvas.drawText("--------------------------------------------------", 40f, 270f, paint)
            
            canvas.drawText("Thank you for your business!", 40f, 330f, paint)
            
            pdfDocument.finishPage(page)
            
            // Save to cache dir
            val invoicesDir = File(context.cacheDir, "invoices")
            if (!invoicesDir.exists()) {
                invoicesDir.mkdirs()
            }
            val file = File(invoicesDir, "invoice_${customer.customerId}.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            
            // 2. Share via WhatsApp
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val number = customer.contactPhone.replace("+", "").replace(" ", "")
            
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/pdf"
            intent.setPackage("com.whatsapp")
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.putExtra("jid", "$number@s.whatsapp.net")
            intent.putExtra(Intent.EXTRA_TEXT, "Hello ${customer.businessName},\n\nHere is your invoice for today's delivery. Total: PKR $amount")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            
            context.startActivity(intent)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
