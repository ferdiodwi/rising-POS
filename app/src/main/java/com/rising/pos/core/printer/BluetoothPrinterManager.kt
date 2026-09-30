package com.rising.pos.core.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.datastore.BusinessSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class BluetoothPrinterDevice(
    val name: String,
    val address: String
)

@Singleton
class BluetoothPrinterManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
    }

    fun isBluetoothAvailable(): Boolean = bluetoothAdapter != null

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun getPairedPrinters(): List<BluetoothPrinterDevice> {
        val adapter = bluetoothAdapter ?: return emptyList()
        if (!adapter.isEnabled) return emptyList()

        return try {
            adapter.bondedDevices.map { device ->
                BluetoothPrinterDevice(
                    name = device.name ?: "Unknown Device",
                    address = device.address
                )
            }
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun printBytes(macAddress: String, bytes: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        val adapter = bluetoothAdapter ?: return@withContext Result.failure(
            IllegalStateException("Bluetooth tidak tersedia di perangkat ini.")
        )

        if (!adapter.isEnabled) {
            return@withContext Result.failure(
                IllegalStateException("Bluetooth sedang nonaktif. Silakan aktifkan Bluetooth.")
            )
        }

        if (macAddress.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Alamat printer belum dipilih di Pengaturan.")
            )
        }

        var socket: BluetoothSocket? = null
        try {
            val device: BluetoothDevice = adapter.getRemoteDevice(macAddress)

            // Cancel discovery as it slows down the connection
            try {
                adapter.cancelDiscovery()
            } catch (_: Exception) {}

            socket = device.createRfcommSocketToServiceRecord(sppUuid)
            socket.connect()

            val outputStream = socket.outputStream
            outputStream.write(bytes)
            outputStream.flush()

            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(IOException("Gagal terhubung ke printer ($macAddress): ${e.localizedMessage}", e))
        } catch (e: SecurityException) {
            Result.failure(SecurityException("Izin Bluetooth belum diberikan di perangkat.", e))
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {}
        }
    }

    suspend fun printReceipt(
        macAddress: String,
        transactionWithDetails: TransactionWithDetails,
        settings: BusinessSettings
    ): Result<Unit> {
        val bytes = EscPosBuilder.buildReceiptBytes(transactionWithDetails, settings)
        return printBytes(macAddress, bytes)
    }

    suspend fun testPrint(
        macAddress: String,
        storeName: String,
        paperWidthMm: Int
    ): Result<Unit> {
        val bytes = EscPosBuilder.buildTestPrintBytes(storeName, paperWidthMm)
        return printBytes(macAddress, bytes)
    }
}
