package com.rising.pos.feature.expense

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.StoreMallDirectory
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.database.entity.ExpenseEntity
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.ui.components.WorkspaceEmptyState
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.DangerRedContainer
import com.rising.pos.ui.theme.PosTextStyles
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pengeluaran operasional.
 *
 * Ringkasan total di atas (satu angka menonjol), daftar di bawah dengan ikon
 * kategori. Form menonjolkan nominal karena itu input paling penting.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val expenses by viewModel.expenses.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val totalExpense = expenses.sumOf { it.amount }
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Button(
                onClick = viewModel::openForm,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Catat pengeluaran", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Pengeluaran", style = MaterialTheme.typography.headlineSmall, color = Slate900)
                Text(
                    "Catat biaya operasional dan belanja toko.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }

            Spacer(Modifier.height(16.dp))

            // Ringkasan total
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DangerRedContainer
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.TrendingDown,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Total pengeluaran tercatat",
                                style = MaterialTheme.typography.labelMedium,
                                color = DangerRed
                            )
                        }
                        Text(
                            CurrencyFormatter.format(totalExpense, settings.currencySymbol),
                            style = PosTextStyles.displayMoney,
                            color = Slate900
                        )
                        Text(
                            "${expenses.size} catatan",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    WorkspaceEmptyState(
                        icon = Icons.Outlined.Payments,
                        title = "Belum ada pengeluaran",
                        description = "Catat biaya seperti belanja bahan, gaji, atau listrik agar laporan usaha akurat."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(expenses, key = { it.id }) { item ->
                        ExpenseItemRow(
                            expense = item,
                            currencySymbol = settings.currencySymbol,
                            dateStr = dateFormatter.format(Date(item.date)),
                            onDelete = { expenseToDelete = item }
                        )
                    }
                }
            }
        }
    }

    if (uiState.isFormOpen) {
        ExpenseFormBottomSheet(
            form = uiState.formState,
            currencySymbol = settings.currencySymbol,
            onCategoryChange = viewModel::updateCategory,
            onAmountChange = viewModel::updateAmount,
            onNotesChange = viewModel::updateNotes,
            onDismiss = viewModel::closeForm,
            onSave = viewModel::saveExpense
        )
    }

    expenseToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Hapus pengeluaran", style = MaterialTheme.typography.titleMedium, color = Slate900) },
            text = {
                Text(
                    "Catatan ${item.category} sebesar ${CurrencyFormatter.format(item.amount, settings.currencySymbol)} akan dihapus.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate700
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpense(item)
                        expenseToDelete = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Hapus", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Batal", style = MaterialTheme.typography.labelLarge, color = Slate700)
                }
            }
        )
    }
}

@Composable
private fun ExpenseItemRow(
    expense: ExpenseEntity,
    currencySymbol: String,
    dateStr: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = DangerRedContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = categoryIcon(expense.category),
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.titleSmall,
                    color = Slate900
                )
                if (!expense.notes.isNullOrBlank()) {
                    Text(
                        text = expense.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.format(expense.amount, currencySymbol),
                    style = PosTextStyles.money,
                    color = DangerRed
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = "Hapus pengeluaran ${expense.category}",
                    tint = Slate500,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun categoryIcon(category: String): ImageVector = when (category) {
    "Bahan Baku" -> Icons.Outlined.Inventory2
    "Operasional" -> Icons.Outlined.Storefront
    "Listrik & Air" -> Icons.Outlined.Bolt
    "Transport / Kurir" -> Icons.Outlined.LocalShipping
    "Gaji / Upah" -> Icons.Outlined.Payments
    "Sewa Tempat" -> Icons.Outlined.StoreMallDirectory
    "Maintenance / Servis" -> Icons.Outlined.Build
    else -> Icons.Outlined.MoreHoriz
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ExpenseFormBottomSheet(
    form: ExpenseFormState,
    currencySymbol: String,
    onCategoryChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val amountValue = form.amount.toLongOrNull() ?: 0L
    val canSave = amountValue > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Catat pengeluaran", style = MaterialTheme.typography.titleLarge, color = Slate900)

            // Nominal paling penting: input besar & menonjol.
            OutlinedTextField(
                value = form.amount,
                onValueChange = onAmountChange,
                label = { Text("Nominal pengeluaran") },
                prefix = { Text("$currencySymbol ", style = PosTextStyles.priceCard) },
                textStyle = PosTextStyles.priceCard,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Text("Kategori", style = MaterialTheme.typography.titleSmall, color = Slate900)

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                defaultExpenseCategories.forEach { cat ->
                    val isSelected = form.category == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategoryChange(cat) },
                        label = { Text(cat) },
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Slate200
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = Slate500,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            OutlinedTextField(
                value = form.notes,
                onValueChange = onNotesChange,
                label = { Text("Catatan (opsional)") },
                placeholder = { Text("Contoh: Beli beras 25kg dan minyak goreng") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = onSave,
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Simpan pengeluaran", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
