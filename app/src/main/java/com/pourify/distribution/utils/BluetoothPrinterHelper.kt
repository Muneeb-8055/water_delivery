package com.pourify.distribution.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.OutputStream
import java.util.UUID

object BluetoothPrinterHelper {

    // Standard SPP UUID for Bluetooth Serial Port Profile
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

    @SuppressLint("MissingPermission")
    fun printReceipt(macAddress: String, customerName: String, amountDue: Double, itemsDelivered: Int, emptiesReturned: Int) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return
        
        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null

        try {
            val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress)
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothAdapter.cancelDiscovery()
            socket.connect()
            outputStream = socket.outputStream

            // Initialize
            outputStream.write(ESC_INIT)

            // Header
            outputStream.write(ESC_ALIGN_CENTER)
            outputStream.write(ESC_DOUBLE_SIZE)
            outputStream.write(ESC_BOLD_ON)
            outputStream.write("TARSIL\n".toByteArray())
            outputStream.write(ESC_BOLD_OFF)
            outputStream.write(ESC_NORMAL_SIZE)
            outputStream.write("WATER DELIVERY\n".toByteArray())
            outputStream.write("--------------------------------\n".toByteArray())

            // Details
            outputStream.write(ESC_ALIGN_LEFT)
            outputStream.write("Customer: $customerName\n".toByteArray())
            outputStream.write("Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}\n".toByteArray())
            outputStream.write("--------------------------------\n".toByteArray())
            
            outputStream.write("Items Delivered: $itemsDelivered\n".toByteArray())
            outputStream.write("Empties Returned: $emptiesReturned\n".toByteArray())
            outputStream.write("--------------------------------\n".toByteArray())
            
            outputStream.write(ESC_DOUBLE_SIZE)
            outputStream.write(ESC_BOLD_ON)
            outputStream.write("Total Due: PKR ${amountDue.toInt()}\n".toByteArray())
            outputStream.write(ESC_BOLD_OFF)
            outputStream.write(ESC_NORMAL_SIZE)
            
            // Footer & Signature space
            outputStream.write("\n".toByteArray())
            outputStream.write("Signature:______________________\n".toByteArray())
            outputStream.write("\n\n\n".toByteArray())
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
