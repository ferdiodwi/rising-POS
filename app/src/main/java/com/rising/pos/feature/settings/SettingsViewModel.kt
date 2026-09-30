package com.rising.pos.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.export.DataExportManager
import com.rising.pos.core.printer.BluetoothPrinterDevice
import com.rising.pos.core.printer.BluetoothPrinterManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val pairedPrinters: List<BluetoothPrinterDevice> = emptyList(),
    val isPrinterPickerOpen: Boolean = false,
    val isTestingPrint: Boolean = false,
    val printerStatusMessage: String? = null,
    val isExporting: Boolean = false,
    val exportStatusMessage: String? = null,
    val isPinSetupDialogOpen: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val printerManager: BluetoothPrinterManager,
    private val dataExportManager: DataExportManager
) : ViewModel() {

    val settings: StateFlow<BusinessSettings> = appPreferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BusinessSettings()
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun openPrinterPicker() {
        val printers = printerManager.getPairedPrinters()
        _uiState.update { it.copy(pairedPrinters = printers, isPrinterPickerOpen = true) }
    }

    fun closePrinterPicker() {
        _uiState.update { it.copy(isPrinterPickerOpen = false) }
    }

    fun selectPrinter(device: BluetoothPrinterDevice) {
        val current = settings.value
        viewModelScope.launch {
            appPreferences.updatePrinterSettings(
                macAddress = device.address,
                name = device.name,
                paperWidthMm = current.printerPaperWidthMm,
                autoPrintReceipt = current.autoPrintReceipt
            )
            _uiState.update { it.copy(isPrinterPickerOpen = false) }
        }
    }

    fun setPaperWidth(widthMm: Int) {
        val current = settings.value
        viewModelScope.launch {
            appPreferences.updatePrinterSettings(
                macAddress = current.printerMacAddress,
                name = current.printerName,
                paperWidthMm = widthMm,
                autoPrintReceipt = current.autoPrintReceipt
            )
        }
    }

    fun setAutoPrint(autoPrint: Boolean) {
        val current = settings.value
        viewModelScope.launch {
            appPreferences.updatePrinterSettings(
                macAddress = current.printerMacAddress,
                name = current.printerName,
                paperWidthMm = current.printerPaperWidthMm,
                autoPrintReceipt = autoPrint
            )
        }
    }

    fun testPrint() {
        val current = settings.value
        if (current.printerMacAddress.isBlank()) {
            _uiState.update { it.copy(printerStatusMessage = "Pilih printer Bluetooth terlebih dahulu.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isTestingPrint = true, printerStatusMessage = null) }
            val result = printerManager.testPrint(
                macAddress = current.printerMacAddress,
                storeName = current.name,
                paperWidthMm = current.printerPaperWidthMm
            )
            result.onSuccess {
                _uiState.update { it.copy(isTestingPrint = false, printerStatusMessage = "Uji cetak berhasil terkirim ke printer.") }
            }.onFailure { err ->
                _uiState.update { it.copy(isTestingPrint = false, printerStatusMessage = "Gagal cetak: ${err.message}") }
            }
        }
    }

    fun clearPrinterStatus() {
        _uiState.update { it.copy(printerStatusMessage = null) }
    }


    fun updateFeatureToggles(
        isTableEnabled: Boolean,
        isModifierEnabled: Boolean,
        isBarcodeEnabled: Boolean,
        isStockTrackingEnabled: Boolean
    ) {
        viewModelScope.launch {
            appPreferences.updateFeatureToggles(
                isTableEnabled = isTableEnabled,
                isModifierEnabled = isModifierEnabled,
                isBarcodeEnabled = isBarcodeEnabled,
                isStockTrackingEnabled = isStockTrackingEnabled
            )
        }
    }

    fun updateTaxAndService(
        isTaxEnabled: Boolean,
        taxPercentage: Double,
        isTaxInclusive: Boolean,
        isServiceChargeEnabled: Boolean,
        serviceChargePercentage: Double
    ) {
        viewModelScope.launch {
            appPreferences.updateTaxAndService(
                isTaxEnabled = isTaxEnabled,
                taxPercentage = taxPercentage,
                isTaxInclusive = isTaxInclusive,
                isServiceChargeEnabled = isServiceChargeEnabled,
                serviceChargePercentage = serviceChargePercentage
            )
        }
    }

    fun updateBusinessProfile(
        name: String,
        phone: String,
        address: String,
        footerNote: String,
        deviceId: String
    ) {
        val current = settings.value
        viewModelScope.launch {
            appPreferences.updateBusinessProfile(
                name = name,
                type = current.type,
                phone = phone,
                address = address,
                currencySymbol = current.currencySymbol,
                footerNote = footerNote,
                deviceId = deviceId
            )
        }
    }

    fun exportTransactions(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportStatusMessage = null) }
            val result = dataExportManager.exportTransactionsCsv(settings.value.currencySymbol)
            result.fold(
                onSuccess = { intent ->
                    _uiState.update { it.copy(isExporting = false) }
                    context.startActivity(intent)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportStatusMessage = "Gagal ekspor: ${err.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun exportProducts(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportStatusMessage = null) }
            val result = dataExportManager.exportProductsCsv(settings.value.currencySymbol)
            result.fold(
                onSuccess = { intent ->
                    _uiState.update { it.copy(isExporting = false) }
                    context.startActivity(intent)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportStatusMessage = "Gagal ekspor produk: ${err.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun exportExpenses(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportStatusMessage = null) }
            val result = dataExportManager.exportExpensesCsv(settings.value.currencySymbol)
            result.fold(
                onSuccess = { intent ->
                    _uiState.update { it.copy(isExporting = false) }
                    context.startActivity(intent)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportStatusMessage = "Gagal ekspor biaya: ${err.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun backupDatabase(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportStatusMessage = null) }
            val result = dataExportManager.createDatabaseBackup()
            result.fold(
                onSuccess = { intent ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportStatusMessage = "Cadangan database berhasil dibuat!"
                        )
                    }
                    context.startActivity(intent)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportStatusMessage = "Gagal membuat cadangan: ${err.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun updatePinSecurity(enabled: Boolean, pin: String) {
        viewModelScope.launch {
            appPreferences.updatePinSecurity(enabled, pin)
            _uiState.update { it.copy(isPinSetupDialogOpen = false) }
        }
    }

    fun openPinSetupDialog() {
        _uiState.update { it.copy(isPinSetupDialogOpen = true) }
    }

    fun closePinSetupDialog() {
        _uiState.update { it.copy(isPinSetupDialogOpen = false) }
    }
}
