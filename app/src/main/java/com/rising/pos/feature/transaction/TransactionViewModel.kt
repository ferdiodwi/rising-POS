package com.rising.pos.feature.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionUiState(
    val selectedTransaction: TransactionWithDetails? = null,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val settings: StateFlow<BusinessSettings> = appPreferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BusinessSettings()
    )

    val transactions: StateFlow<List<TransactionWithDetails>> =
        transactionRepository.getRecentTransactions(limit = 100).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState = _uiState.asStateFlow()

    fun selectTransaction(transaction: TransactionWithDetails?) {
        _uiState.update { it.copy(selectedTransaction = transaction) }
    }

    fun voidTransaction(transactionId: String, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            val result = transactionRepository.voidTransaction(transactionId, reason)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        selectedTransaction = null,
                        successMessage = "Transaksi berhasil dibatalkan (VOID) dan stok dikembalikan."
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = err.message ?: "Gagal membatalkan transaksi"
                    )
                }
            }
        }
    }

    fun refundTransaction(transactionId: String, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            val result = transactionRepository.refundTransaction(transactionId, reason)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        selectedTransaction = null,
                        successMessage = "Transaksi berhasil di-refund dan stok dikembalikan."
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = err.message ?: "Gagal me-refund transaksi"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}

