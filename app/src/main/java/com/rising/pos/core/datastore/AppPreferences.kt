package com.rising.pos.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rising.pos.core.model.AppTheme
import com.rising.pos.core.model.BusinessType
import com.rising.pos.core.security.PinHasher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    val serviceChargePercentage: Double = 5.0,
    // Printer
    val printerMacAddress: String = "",
    val printerName: String = "",
    val printerPaperWidthMm: Int = 58,
    val autoPrintReceipt: Boolean = false,
    // Security
    val isPinSecurityEnabled: Boolean = false,
    /** PBKDF2-HMAC-SHA256 hash (Base64) dari PIN Owner. Bukan PIN itu sendiri. */
    val securityPinHash: String = "",
    /** Salt acak (Base64) untuk [securityPinHash]. */
    val securityPinSalt: String = "",
    // Appearance / Theme
    val appTheme: AppTheme = AppTheme.SYSTEM
) {
    /** True bila PIN Owner sudah diatur (hash tersimpan). */
    val hasPin: Boolean get() = securityPinHash.isNotBlank() && securityPinSalt.isNotBlank()
}

@Singleton
class AppPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
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

        val PRINTER_MAC_ADDRESS = stringPreferencesKey("printer_mac_address")
        val PRINTER_NAME = stringPreferencesKey("printer_name")
        val PRINTER_PAPER_WIDTH = androidx.datastore.preferences.core.intPreferencesKey("printer_paper_width")
        val AUTO_PRINT_RECEIPT = booleanPreferencesKey("auto_print_receipt")

        val IS_PIN_SECURITY_ENABLED = booleanPreferencesKey("is_pin_security_enabled")
        val SECURITY_PIN_HASH = stringPreferencesKey("security_pin_hash")
        val SECURITY_PIN_SALT = stringPreferencesKey("security_pin_salt")
        val APP_THEME = stringPreferencesKey("app_theme")
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
            serviceChargePercentage = prefs[Keys.SERVICE_CHARGE_PERCENTAGE] ?: 5.0,
            printerMacAddress = prefs[Keys.PRINTER_MAC_ADDRESS] ?: "",
            printerName = prefs[Keys.PRINTER_NAME] ?: "",
            printerPaperWidthMm = prefs[Keys.PRINTER_PAPER_WIDTH] ?: 58,
            autoPrintReceipt = prefs[Keys.AUTO_PRINT_RECEIPT] ?: false,
            isPinSecurityEnabled = prefs[Keys.IS_PIN_SECURITY_ENABLED] ?: false,
            securityPinHash = prefs[Keys.SECURITY_PIN_HASH] ?: "",
            securityPinSalt = prefs[Keys.SECURITY_PIN_SALT] ?: "",
            appTheme = try {
                AppTheme.valueOf(prefs[Keys.APP_THEME] ?: AppTheme.SYSTEM.name)
            } catch (_: Exception) {
                AppTheme.SYSTEM
            }
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

    suspend fun updatePrinterSettings(
        macAddress: String,
        name: String,
        paperWidthMm: Int,
        autoPrintReceipt: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PRINTER_MAC_ADDRESS] = macAddress
            prefs[Keys.PRINTER_NAME] = name
            prefs[Keys.PRINTER_PAPER_WIDTH] = paperWidthMm
            prefs[Keys.AUTO_PRINT_RECEIPT] = autoPrintReceipt
        }
    }

    /**
     * Menyimpan setelan PIN. [pin] di-hash dengan PBKDF2-HMAC-SHA256 + salt acak baru;
     * PIN mentah tidak pernah ditulis ke disk. Bila [enabled] = false, hash & salt dihapus.
     */
    suspend fun updatePinSecurity(enabled: Boolean, pin: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_PIN_SECURITY_ENABLED] = enabled
            if (enabled && pin.isNotBlank()) {
                val salt = PinHasher.newSalt()
                prefs[Keys.SECURITY_PIN_HASH] = PinHasher.hash(pin, salt)
                prefs[Keys.SECURITY_PIN_SALT] = salt
            } else if (!enabled) {
                prefs.remove(Keys.SECURITY_PIN_HASH)
                prefs.remove(Keys.SECURITY_PIN_SALT)
            }
        }
    }

    /** Memverifikasi [pin] terhadap hash tersimpan. False bila PIN belum diatur. */
    suspend fun verifyPin(pin: String): Boolean {
        val prefs = context.dataStore.data.first()
        val hash = prefs[Keys.SECURITY_PIN_HASH] ?: return false
        val salt = prefs[Keys.SECURITY_PIN_SALT] ?: return false
        return PinHasher.verify(pin, hash, salt)
    }

    suspend fun updateAppTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_THEME] = theme.name
        }
    }
}

