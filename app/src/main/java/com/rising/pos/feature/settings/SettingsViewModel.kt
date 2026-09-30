package com.rising.pos.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    val settings: StateFlow<BusinessSettings> = appPreferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BusinessSettings()
    )

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
}
