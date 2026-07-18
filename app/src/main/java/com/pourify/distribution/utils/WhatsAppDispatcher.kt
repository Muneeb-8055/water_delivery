package com.pourify.distribution.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.net.URLEncoder

object WhatsAppDispatcher {
    fun sendInvoiceText(context: Context, phoneNumber: String, balanceDue: Double) {
        val number = phoneNumber.replace("+", "").replace(" ", "")
        val message = "Hello, here is your Pourify delivery invoice. Balance Due: PKR $balanceDue"
        val encodedMessage = URLEncoder.encode(message, "UTF-8")
        
        val uri = Uri.parse("whatsapp://send?phone=$number&text=$encodedMessage")
        val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, message) }
        
        try {
            context.startActivity(Intent.createChooser(intent, "Share Invoice via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun sendPdfViaWhatsApp(context: Context, phoneNumber: String, pdfFile: File, message: String) {
        val number = phoneNumber.replace("+", "").replace(" ", "")
        
        val uri = try {
            FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                pdfFile
            )
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra("jid", "$number@s.whatsapp.net") // Direct specific number
            // setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Share Invoice via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
