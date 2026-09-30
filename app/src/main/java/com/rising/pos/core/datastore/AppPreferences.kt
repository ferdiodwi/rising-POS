package com.rising.pos.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rising.pos.core.model.BusinessType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pos_settings")

data class BusinessSettings(
    val name: String = "Toko Saya",
    val type: BusinessType = BusinessType.WARUNG,
    val phone: String = "",
    val address: String = "",
    val currencySymbol: String = "Rp",
    val footerNote: String = "Terima kasih atas kunjungan Anda!",
    val deviceId: String = "A01",
    val cashierName: String = "Kasir",
    val isOnboardingCompleted: Boolean = false,
    // Feature toggles
    val isTableEnabled: Boolean = false,
    val isModifierEnabled: Boolean = false,
    val isBarcodeEnabled: Boolean = true,
    val isStockTrackingEnabled: Boolean = true,
    // Tax & Service Charge
    val isTaxEnabled: Boolean = false,
    val taxPercentage: Double = 11.0,
    val isTaxInclusive: Boolean = true,
    val isServiceChargeEnabled: Boolean = false,
    val serviceChargePercentage: Double = 5.0
)

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val BUSINESS_NAME = stringPreferencesKey("business_name")
        val BUSINESS_TYPE = stringPreferencesKey("business_type")
        val BUSINESS_PHONE = stringPreferencesKey("business_phone")
        val BUSINESS_ADDRESS = stringPreferencesKey("business_address")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val FOOTER_NOTE = stringPreferencesKey("footer_note")
        val DEVICE_ID = stringPreferencesKey("device_id")
        val CASHIER_NAME = stringPreferencesKey("cashier_name")

        val IS_TABLE_ENABLED = booleanPreferencesKey("is_table_enabled")
        val IS_MODIFIER_ENABLED = booleanPreferencesKey("is_modifier_enabled")
        val IS_BARCODE_ENABLED = booleanPreferencesKey("is_barcode_enabled")
        val IS_STOCK_TRACKING_ENABLED = booleanPreferencesKey("is_stock_tracking_enabled")

        val IS_TAX_ENABLED = booleanPreferencesKey("is_tax_enabled")
        val TAX_PERCENTAGE = doublePreferencesKey("tax_percentage")
        val IS_TAX_INCLUSIVE = booleanPreferencesKey("is_tax_inclusive")
        val IS_SERVICE_CHARGE_ENABLED = booleanPreferencesKey("is_service_charge_enabled")
        val SERVICE_CHARGE_PERCENTAGE = doublePreferencesKey("service_charge_percentage")
    }

    val settingsFlow: Flow<BusinessSettings> = context.dataStore.data.map { prefs ->
        val typeString = prefs[Keys.BUSINESS_TYPE] ?: BusinessType.WARUNG.name
        val businessType = try {
            BusinessType.valueOf(typeString)
        } catch (_: Exception) {
            BusinessType.WARUNG
        }

        BusinessSettings(
            name = prefs[Keys.BUSINESS_NAME] ?: "Toko Saya",
            type = businessType,
            phone = prefs[Keys.BUSINESS_PHONE] ?: "",
            address = prefs[Keys.BUSINESS_ADDRESS] ?: "",
            currencySymbol = prefs[Keys.CURRENCY_SYMBOL] ?: "Rp",
            footerNote = prefs[Keys.FOOTER_NOTE] ?: "Terima kasih atas kunjungan Anda!",
            deviceId = prefs[Keys.DEVICE_ID] ?: "A01",
            cashierName = prefs[Keys.CASHIER_NAME] ?: "Kasir",
            isOnboardingCompleted = prefs[Keys.IS_ONBOARDING_COMPLETED] ?: false,
            isTableEnabled = prefs[Keys.IS_TABLE_ENABLED] ?: false,
            isModifierEnabled = prefs[Keys.IS_MODIFIER_ENABLED] ?: false,
            isBarcodeEnabled = prefs[Keys.IS_BARCODE_ENABLED] ?: true,
            isStockTrackingEnabled = prefs[Keys.IS_STOCK_TRACKING_ENABLED] ?: true,
            isTaxEnabled = prefs[Keys.IS_TAX_ENABLED] ?: false,
            taxPercentage = prefs[Keys.TAX_PERCENTAGE] ?: 11.0,
            isTaxInclusive = prefs[Keys.IS_TAX_INCLUSIVE] ?: true,
            isServiceChargeEnabled = prefs[Keys.IS_SERVICE_CHARGE_ENABLED] ?: false,
            serviceChargePercentage = prefs[Keys.SERVICE_CHARGE_PERCENTAGE] ?: 5.0
        )
    }

    suspend fun updateBusinessProfile(
        name: String,
        type: BusinessType,
        phone: String,
        address: String,
        currencySymbol: String,
        footerNote: String,
        deviceId: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BUSINESS_NAME] = name
            prefs[Keys.BUSINESS_TYPE] = type.name
            prefs[Keys.BUSINESS_PHONE] = phone
            prefs[Keys.BUSINESS_ADDRESS] = address
            prefs[Keys.CURRENCY_SYMBOL] = currencySymbol
            prefs[Keys.FOOTER_NOTE] = footerNote
            prefs[Keys.DEVICE_ID] = deviceId
            prefs[Keys.IS_ONBOARDING_COMPLETED] = true

            // Automatically configure default feature toggles based on business type if not yet configured
            when (type) {
                BusinessType.WARUNG -> {
                    prefs[Keys.IS_TABLE_ENABLED] = false
                    prefs[Keys.IS_MODIFIER_ENABLED] = false
                    prefs[Keys.IS_BARCODE_ENABLED] = true
                    prefs[Keys.IS_STOCK_TRACKING_ENABLED] = true
                }
                BusinessType.CAFE -> {
                    prefs[Keys.IS_TABLE_ENABLED] = true
                    prefs[Keys.IS_MODIFIER_ENABLED] = true
                    prefs[Keys.IS_BARCODE_ENABLED] = false
                    prefs[Keys.IS_STOCK_TRACKING_ENABLED] = true
                }
                BusinessType.RETAIL -> {
                    prefs[Keys.IS_TABLE_ENABLED] = false
                    prefs[Keys.IS_MODIFIER_ENABLED] = false
                    prefs[Keys.IS_BARCODE_ENABLED] = true
                    prefs[Keys.IS_STOCK_TRACKING_ENABLED] = true
                }
                BusinessType.SERVICE -> {
                    prefs[Keys.IS_TABLE_ENABLED] = false
                    prefs[Keys.IS_MODIFIER_ENABLED] = false
                    prefs[Keys.IS_BARCODE_ENABLED] = false
                    prefs[Keys.IS_STOCK_TRACKING_ENABLED] = false
                }
                else -> { /* retain custom */ }
            }
        }
    }

    suspend fun updateFeatureToggles(
        isTableEnabled: Boolean,
        isModifierEnabled: Boolean,
        isBarcodeEnabled: Boolean,
        isStockTrackingEnabled: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_TABLE_ENABLED] = isTableEnabled
            prefs[Keys.IS_MODIFIER_ENABLED] = isModifierEnabled
            prefs[Keys.IS_BARCODE_ENABLED] = isBarcodeEnabled
            prefs[Keys.IS_STOCK_TRACKING_ENABLED] = isStockTrackingEnabled
        }
    }

    suspend fun updateTaxAndService(
        isTaxEnabled: Boolean,
        taxPercentage: Double,
        isTaxInclusive: Boolean,
        isServiceChargeEnabled: Boolean,
        serviceChargePercentage: Double
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_TAX_ENABLED] = isTaxEnabled
            prefs[Keys.TAX_PERCENTAGE] = taxPercentage
            prefs[Keys.IS_TAX_INCLUSIVE] = isTaxInclusive
            prefs[Keys.IS_SERVICE_CHARGE_ENABLED] = isServiceChargeEnabled
            prefs[Keys.SERVICE_CHARGE_PERCENTAGE] = serviceChargePercentage
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_ONBOARDING_COMPLETED] = completed
        }
    }
}
