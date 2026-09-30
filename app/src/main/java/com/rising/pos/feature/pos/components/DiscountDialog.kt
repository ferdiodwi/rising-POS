package com.rising.pos.feature.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen

enum class DiscountType {
    NOMINAL,
    PERCENTAGE
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscountDialog(
    subtotal: Double,
    currencySymbol: String,
    currentDiscount: Double,
    currentReason: String?,
    onDismiss: () -> Unit,
    onApplyDiscount: (amount: Double, reason: String?) -> Unit
) {
    var discountType by remember { mutableStateOf(DiscountType.NOMINAL) }
    var inputValue by remember {
        mutableStateOf(if (currentDiscount > 0) currentDiscount.toInt().toString() else "")
    }
    var reason by remember { mutableStateOf(currentReason ?: "") }

    val rawInput = inputValue.toDoubleOrNull() ?: 0.0

    val calculatedDiscountAmount = when (discountType) {
        DiscountType.NOMINAL -> minOf(rawInput, subtotal)
        DiscountType.PERCENTAGE -> {
            val clampedPercentage = minOf(100.0, maxOf(0.0, rawInput))
            (subtotal * clampedPercentage) / 100.0
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(DangerRed.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Discount,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Diskon Pesanan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Discount Type Selector (Nominal / Percentage)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = discountType == DiscountType.NOMINAL,
                        onClick = {
                            discountType = DiscountType.NOMINAL
                            inputValue = ""
                        },
                        label = { Text("Nominal ($currencySymbol)") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                            selectedLabelColor = PrimaryBlue
                        )
                    )

                    FilterChip(
                        selected = discountType == DiscountType.PERCENTAGE,
                        onClick = {
                            discountType = DiscountType.PERCENTAGE
                            inputValue = ""
                        },
                        label = { Text("Persentase (%)") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                            selectedLabelColor = PrimaryBlue
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick percentage chips
                if (discountType == DiscountType.PERCENTAGE) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 20, 25, 50).forEach { pct ->
                            FilterChip(
                                selected = inputValue == pct.toString(),
                                onClick = { inputValue = pct.toString() },
                                label = { Text("$pct%") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SuccessGreen.copy(alpha = 0.15f),
                                    selectedLabelColor = SuccessGreen
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Input value field
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' }) {
                            inputValue = it
                        }
                    },
                    label = {
                        Text(if (discountType == DiscountType.NOMINAL) "Nominal Potongan ($currencySymbol)" else "Persen Potongan (%)")
                    },
                    placeholder = {
                        Text(if (discountType == DiscountType.NOMINAL) "Contoh: 10000" else "Contoh: 10")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Reason for discount
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Alasan Diskon (Opsional)") },
                    placeholder = { Text("Contoh: Promo Pembukaan, Member, Teman") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = Slate500) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Calculation Preview Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Slate200.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal:", style = MaterialTheme.typography.bodySmall.copy(color = Slate500))
                            Text(CurrencyFormatter.format(subtotal, currencySymbol), style = MaterialTheme.typography.bodySmall.copy(color = Slate900, fontWeight = FontWeight.Medium))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Diskon:", style = MaterialTheme.typography.bodySmall.copy(color = DangerRed))
                            Text("- ${CurrencyFormatter.format(calculatedDiscountAmount, currencySymbol)}", style = MaterialTheme.typography.bodySmall.copy(color = DangerRed, fontWeight = FontWeight.Bold))
                        }
                        HorizontalDivider(color = Slate200, modifier = Modifier.padding(vertical = 2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal Bersih:", style = MaterialTheme.typography.bodyMedium.copy(color = Slate900, fontWeight = FontWeight.Bold))
                            Text(
                                CurrencyFormatter.format(maxOf(0.0, subtotal - calculatedDiscountAmount), currencySymbol),
                                style = MaterialTheme.typography.bodyMedium.copy(color = SuccessGreen, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (currentDiscount > 0) {
                        OutlinedButton(
                            onClick = {
                                onApplyDiscount(0.0, null)
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hapus", fontSize = 13.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            onApplyDiscount(calculatedDiscountAmount, reason.trim().takeIf { it.isNotBlank() })
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Terapkan")
                    }
                }
            }
        }
    }
}
