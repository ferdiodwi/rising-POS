package com.rising.pos.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.model.AppTheme
import com.rising.pos.core.export.DataExportManager
import com.rising.pos.core.printer.BluetoothPrinterDevice
import com.rising.pos.core.printer.BluetoothPrinterManager
import android.content.Intent
import com.rising.pos.core.update.AppUpdateManager
import com.rising.pos.core.update.model.UpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.rising.pos.core.database.entity.RestaurantTableEntity
import com.rising.pos.domain.repository.SyncRepository
import com.rising.pos.domain.repository.TableRepository

data class SettingsUiState(
    val isSavingProfile: Boolean = false,
    val profileStatusMessage: String? = null,
    val pairedPrinters: List<BluetoothPrinterDevice> = emptyList(),
    val isPrinterPickerOpen: Boolean = false,
    val isTestingPrint: Boolean = false,
    val printerStatusMessage: String? = null,
    val isExporting: Boolean = false,
    val exportStatusMessage: String? = null,
    val isRestoring: Boolean = false,
    val restoreStatusMessage: String? = null,
    val isRestoreSuccess: Boolean = false,
    val isPinSetupDialogOpen: Boolean = false,
    val isTableManagementDialogOpen: Boolean = false,
    val isSyncing: Boolean = false,
    val syncStatusMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val printerManager: BluetoothPrinterManager,
    private val dataExportManager: DataExportManager,
    private val appUpdateManager: AppUpdateManager,
    private val tableRepository: TableRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    val updateState: StateFlow<UpdateState> = appUpdateManager.updateState

    val settings: StateFlow<BusinessSettings> = appPreferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BusinessSettings()
    )

    val tables: StateFlow<List<RestaurantTableEntity>> = tableRepository.getAllTables().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val syncPendingCount: StateFlow<Int> = syncRepository.getPendingCount().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    init {
        syncRepository.schedulePeriodicSync()
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun openTableManagement() {
        _uiState.update { it.copy(isTableManagementDialogOpen = true) }
    }

    fun closeTableManagement() {
        _uiState.update { it.copy(isTableManagementDialogOpen = false) }
    }

    fun saveTable(table: RestaurantTableEntity) {
        viewModelScope.launch {
            tableRepository.saveTable(table)
        }
    }

    fun deleteTable(table: RestaurantTableEntity) {
        viewModelScope.launch {
            tableRepository.deleteTable(table)
        }
    }

    fun toggleTableOccupied(tableNumber: String, isOccupied: Boolean) {
        viewModelScope.launch {
            tableRepository.updateOccupiedStatus(tableNumber, isOccupied)
        }
    }

    fun seedDefaultTables() {
        viewModelScope.launch {
            tableRepository.seedDefaultTablesIfEmpty()
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncStatusMessage = null) }
            val result = syncRepository.syncNow()
            result.fold(
                onSuccess = { syncResult ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            syncStatusMessage = syncResult.message
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            syncStatusMessage = error.message ?: "Gagal melakukan sinkronisasi"
                        )
                    }
                }
            )
        }
    }

    fun dismissSyncMessage() {
        _uiState.update { it.copy(syncStatusMessage = null) }
    }

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

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            appPreferences.updateAppTheme(theme)
        }
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
        deviceId: String,
        email: String? = null,
        logoUrl: String? = null
    ) {
        if (_uiState.value.isSavingProfile || name.isBlank()) return
        val current = settings.value
        _uiState.update { it.copy(isSavingProfile = true, profileStatusMessage = null) }
        viewModelScope.launch {
            runCatching { appPreferences.updateBusinessProfile(
                name = name,
                type = current.type,
                phone = phone,
                address = address,
                currencySymbol = current.currencySymbol,
                footerNote = footerNote,
                deviceId = deviceId,
                email = email,
                logoUrl = logoUrl
            ) }.fold(
                onSuccess = { _uiState.update { it.copy(isSavingProfile = false, profileStatusMessage = "Perubahan terakhir tersimpan.") } },
                onFailure = { error -> _uiState.update { it.copy(isSavingProfile = false, profileStatusMessage = error.message ?: "Gagal menyimpan perubahan.") } }
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

    fun exportCustomers(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportStatusMessage = null) }
            val result = dataExportManager.exportCustomersCsv(settings.value.currencySymbol)
            result.fold(
                onSuccess = { intent ->
                    _uiState.update { it.copy(isExporting = false) }
                    context.startActivity(intent)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportStatusMessage = "Gagal ekspor pelanggan: ${err.localizedMessage}"
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

    fun restoreDatabase(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRestoring = true,
                    restoreStatusMessage = null,
                    isRestoreSuccess = false
                )
            }
            val result = dataExportManager.restoreDatabaseFromUri(uri)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isRestoring = false,
                            isRestoreSuccess = true,
                            restoreStatusMessage = "Database berhasil dipulihkan! Semua data telah diperbarui dari cadangan."
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isRestoring = false,
                            isRestoreSuccess = false,
                            restoreStatusMessage = "Gagal memulihkan database: ${err.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun clearRestoreStatus() {
        _uiState.update { it.copy(restoreStatusMessage = null, isRestoreSuccess = false) }
    }

    fun clearExportStatus() {
        _uiState.update { it.copy(exportStatusMessage = null) }
    }

    fun updatePinSecurity(enabled: Boolean, pin: String) {
        viewModelScope.launch {
            appPreferences.updatePinSecurity(enabled, pin)
            _uiState.update { it.copy(isPinSetupDialogOpen = false) }
        }
    }

    /** Verifikasi PIN Owner (hash PBKDF2). Dipakai oleh gerbang Ubah PIN & Pulihkan DB. */
    suspend fun verifyPin(pin: String): Boolean = appPreferences.verifyPin(pin)

    fun openPinSetupDialog() {
        _uiState.update { it.copy(isPinSetupDialogOpen = true) }
    }

    fun closePinSetupDialog() {
        _uiState.update { it.copy(isPinSetupDialogOpen = false) }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            appUpdateManager.checkForUpdates()
        }
    }

    fun downloadUpdate(downloadUrl: String, versionTag: String, isForceUpdate: Boolean = false) {
        viewModelScope.launch {
            appUpdateManager.downloadApk(downloadUrl, versionTag, isForceUpdate)
        }
    }

    fun installUpdate() {
        appUpdateManager.installApk()
    }

    fun canInstallPackages(): Boolean = appUpdateManager.canInstallPackages()

    fun getUnknownSourcesSettingsIntent(): Intent? = appUpdateManager.getUnknownSourcesSettingsIntent()

    fun dismissUpdate() {
        appUpdateManager.resetState()
    }
}
