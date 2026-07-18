package com.tarsil.distribution.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

object WhatsAppDispatcher {

    fun sendInvoiceText(context: Context, phoneNumber: String, balanceDue: Double) {
        val number = phoneNumber.replace("+", "").replace(" ", "")
        val message = "Hello, here is your Tarsil delivery invoice. Balance Due: PKR $balanceDue"
        val encodedMessage = URLEncoder.encode(message, "UTF-8")
        
        val uri = Uri.parse("whatsapp://send?phone=$number&text=$encodedMessage")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // In a real app, you would show a Toast here that WhatsApp is not installed.
        }
    }
}
