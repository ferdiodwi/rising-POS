package com.rising.pos.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rising.pos.core.database.entity.CategoryEntity
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.model.BusinessType
import com.rising.pos.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class OnboardingUiState(
    val businessName: String = "",
    val businessType: BusinessType = BusinessType.WARUNG,
    val phone: String = "",
    val address: String = "",
    val currencySymbol: String = "Rp",
    val footerNote: String = "Terima kasih telah berbelanja!",
    val deviceId: String = "A01",
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun updateName(name: String) = _uiState.update { it.copy(businessName = name) }
    fun updateType(type: BusinessType) = _uiState.update { it.copy(businessType = type) }
    fun updatePhone(phone: String) {
        val digits = phone.filter { it.isDigit() }
        val clean = if (digits.startsWith("62")) {
            digits.removePrefix("62")
        } else if (digits.startsWith("0")) {
            digits.removePrefix("0")
        } else {
            digits
        }
        _uiState.update { it.copy(phone = clean) }
    }
    fun updateAddress(address: String) = _uiState.update { it.copy(address = address) }
    fun updateDeviceId(id: String) = _uiState.update { it.copy(deviceId = id) }

    fun submitOnboarding(onSuccess: () -> Unit) {
        val state = _uiState.value
        val name = state.businessName.trim().ifEmpty { "Toko Saya" }
        val phoneToSave = if (state.phone.isNotBlank()) "+62${state.phone.trim()}" else ""
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            appPreferences.updateBusinessProfile(
                name = name,
                type = state.businessType,
                phone = phoneToSave,
                address = state.address.trim(),
                currencySymbol = state.currencySymbol,
                footerNote = state.footerNote,
                deviceId = state.deviceId.trim().ifEmpty { "A01" }
            )

            // Seed default starter categories based on business type
            val defaultCategories = when (state.businessType) {
                BusinessType.CAFE, BusinessType.WARUNG -> listOf("Makanan", "Minuman", "Camilan / Snack")
                BusinessType.RETAIL -> listOf("Kebutuhan Rumah", "Sembako", "Makanan Ringan", "Minuman")
                BusinessType.SERVICE -> listOf("Layanan Utama", "Jasa Tambahan")
                else -> listOf("Umum")
            }

            try {
                defaultCategories.forEachIndexed { index, catName ->
                    productRepository.saveCategory(
                        CategoryEntity(
                            id = UUID.randomUUID().toString(),
                            name = catName,
                            sortOrder = index,
                            isActive = true
                        )
                    )
                }
            } catch (_: Exception) {
                // Ignore if category already exists or fails to seed
            }

            _uiState.update { it.copy(isLoading = false, isCompleted = true) }
            onSuccess()
        }
    }
}
