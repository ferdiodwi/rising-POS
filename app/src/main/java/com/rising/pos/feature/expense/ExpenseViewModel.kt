package com.rising.pos.feature.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rising.pos.core.database.entity.ExpenseEntity
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ExpenseFormState(
    val category: String = "Bahan Baku",
    val amount: String = "",
    val notes: String = ""
)

data class ExpenseUiState(
    val isFormOpen: Boolean = false,
    val formState: ExpenseFormState = ExpenseFormState()
)

val defaultExpenseCategories = listOf(
    "Bahan Baku",
    "Operasional",
    "Listrik & Air",
    "Transport / Kurir",
    "Gaji / Upah",
    "Sewa Tempat",
    "Maintenance / Servis",
    "Lain-lain"
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val settings: StateFlow<BusinessSettings> = appPreferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BusinessSettings()
    )

    val expenses: StateFlow<List<ExpenseEntity>> = expenseRepository.getAllExpenses().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState = _uiState.asStateFlow()

    fun openForm() = _uiState.update { it.copy(isFormOpen = true, formState = ExpenseFormState()) }
    fun closeForm() = _uiState.update { it.copy(isFormOpen = false) }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(formState = it.formState.copy(category = category)) }
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(formState = it.formState.copy(amount = amount.filter { ch -> ch.isDigit() })) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(formState = it.formState.copy(notes = notes)) }
    }

    fun saveExpense() {
        val form = _uiState.value.formState
        val amount = form.amount.toDoubleOrNull() ?: 0.0
        if (amount <= 0) return

        viewModelScope.launch {
            expenseRepository.saveExpense(
                ExpenseEntity(
                    id = UUID.randomUUID().toString(),
                    category = form.category,
                    amount = amount,
                    date = System.currentTimeMillis(),
                    notes = form.notes.trim().ifEmpty { null }
                )
            )
            _uiState.update { it.copy(isFormOpen = false) }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
        }
    }
}
