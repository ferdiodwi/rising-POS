package com.rising.pos.feature.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rising.pos.core.database.entity.CustomerEntity
import com.rising.pos.core.database.entity.CustomerWithStats
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CustomerFormState(
    val isOpen: Boolean = false,
    val editingCustomerId: String? = null,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val errorMessage: String? = null
)

data class CustomerUiState(
    val searchQuery: String = "",
    val formState: CustomerFormState = CustomerFormState(),
    val selectedCustomer: CustomerWithStats? = null,
    val isDeleting: Boolean = false,
    val customerToDelete: CustomerEntity? = null,
    val userMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val settings: StateFlow<BusinessSettings> = appPreferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BusinessSettings()
    )

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    val customers: StateFlow<List<CustomerWithStats>> = _uiState
        .flatMapLatest { state ->
            if (state.searchQuery.isBlank()) {
                customerRepository.getAllCustomersWithStats()
            } else {
                customerRepository.searchCustomersWithStats(state.searchQuery)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedCustomerTransactions: StateFlow<List<TransactionWithDetails>> = _uiState
        .flatMapLatest { state ->
            val cust = state.selectedCustomer
            if (cust != null) {
                customerRepository.getCustomerTransactions(cust.customer.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openAddCustomer() {
        _uiState.update {
            it.copy(formState = CustomerFormState(isOpen = true))
        }
    }

    fun openEditCustomer(customer: CustomerEntity) {
        _uiState.update {
            it.copy(
                formState = CustomerFormState(
                    isOpen = true,
                    editingCustomerId = customer.id,
                    name = customer.name,
                    phone = customer.phone ?: "",
                    email = customer.email ?: "",
                    address = customer.address ?: "",
                    notes = customer.notes ?: ""
                )
            )
        }
    }

    fun closeForm() {
        _uiState.update {
            it.copy(formState = CustomerFormState(isOpen = false))
        }
    }

    fun onFormNameChange(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(name = value, errorMessage = null)) }
    }

    fun onFormPhoneChange(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(phone = value)) }
    }

    fun onFormEmailChange(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(email = value)) }
    }

    fun onFormAddressChange(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(address = value)) }
    }

    fun onFormNotesChange(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(notes = value)) }
    }

    fun saveCustomer() {
        val form = _uiState.value.formState
        val trimmedName = form.name.trim()
        if (trimmedName.isBlank()) {
            _uiState.update { it.copy(formState = form.copy(errorMessage = "Nama pelanggan wajib diisi")) }
            return
        }

        viewModelScope.launch {
            val entity = CustomerEntity(
                id = form.editingCustomerId ?: UUID.randomUUID().toString(),
                name = trimmedName,
                phone = form.phone.trim().takeIf { it.isNotBlank() },
                email = form.email.trim().takeIf { it.isNotBlank() },
                address = form.address.trim().takeIf { it.isNotBlank() },
                notes = form.notes.trim().takeIf { it.isNotBlank() }
            )

            customerRepository.saveCustomer(entity).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            formState = CustomerFormState(isOpen = false),
                            userMessage = if (form.editingCustomerId != null) "Pelanggan berhasil diperbarui" else "Pelanggan baru berhasil ditambahkan"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(formState = form.copy(errorMessage = error.localizedMessage ?: "Gagal menyimpan pelanggan"))
                    }
                }
            )
        }
    }

    fun selectCustomer(customerWithStats: CustomerWithStats?) {
        _uiState.update { it.copy(selectedCustomer = customerWithStats) }
    }

    fun confirmDeleteCustomer(customer: CustomerEntity) {
        _uiState.update { it.copy(customerToDelete = customer) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(customerToDelete = null) }
    }

    fun deleteConfirmedCustomer() {
        val toDelete = _uiState.value.customerToDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            customerRepository.deleteCustomer(toDelete).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            customerToDelete = null,
                            selectedCustomer = if (it.selectedCustomer?.customer?.id == toDelete.id) null else it.selectedCustomer,
                            userMessage = "Pelanggan berhasil dihapus"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            customerToDelete = null,
                            userMessage = "Gagal menghapus: ${error.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
