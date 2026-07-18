package com.pourify.distribution.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.OutputStream
import java.util.UUID

object BluetoothPrinterService {

    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // ESC/POS Commands
    private val ESC_INIT = byteArrayOf(0x1B, 0x40) // Initialize printer
    private val ESC_ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
    private val ESC_ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
    private val ESC_BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
    private val ESC_BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
    private val ESC_DOUBLE_SIZE = byteArrayOf(0x1D, 0x21, 0x11)
    private val ESC_NORMAL_SIZE = byteArrayOf(0x1D, 0x21, 0x00)
    private val LF = byteArrayOf(0x0A)
    private val PAPER_CUT = byteArrayOf(0x1D, 0x56, 0x41, 0x00) // \x1D\x56\x41\x00

    @SuppressLint("MissingPermission")
    fun printInvoice(macAddress: String, customerName: String, items: Int, total: Double, collected: Double, emptiesBalance: Int) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return
        
        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null

        try {
            val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress)
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothAdapter.cancelDiscovery()
            socket.connect()
            outputStream = socket.outputStream

            outputStream.write(ESC_INIT)

            // Header
            outputStream.write(ESC_ALIGN_CENTER)
            outputStream.write(ESC_DOUBLE_SIZE)
            outputStream.write(ESC_BOLD_ON)
            outputStream.write("TARSIL ERP - DELIVERY INVOICE\n".toByteArray())
            outputStream.write(ESC_BOLD_OFF)
            outputStream.write(ESC_NORMAL_SIZE)
            outputStream.write("--------------------------------\n".toByteArray())

            // Body
            outputStream.write(ESC_ALIGN_LEFT)
            outputStream.write("Customer: $customerName\n".toByteArray())
            outputStream.write("Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}\n".toByteArray())
            outputStream.write("--------------------------------\n".toByteArray())
            
            outputStream.write("Items Delivered: $items\n".toByteArray())
            outputStream.write("Total Balance Due: PKR ${total.toInt()}\n".toByteArray())
            outputStream.write("Cash Collected: PKR ${collected.toInt()}\n".toByteArray())
            outputStream.write("Empties Balance: $emptiesBalance Units\n".toByteArray())
            outputStream.write("--------------------------------\n".toByteArray())
            
            outputStream.write("\n".toByteArray())
            outputStream.write("Signature:______________________\n".toByteArray())
            outputStream.write("\n\n\n".toByteArray())
            
            // Paper Cut
            outputStream.write(PAPER_CUT)
            outputStream.write(LF)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                outputStream?.close()
                socket?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
