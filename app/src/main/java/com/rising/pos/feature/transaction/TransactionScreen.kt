package com.rising.pos.feature.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.feature.pos.components.ReceiptSuccessDialog
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextDecoration
import com.rising.pos.core.model.TransactionStatus
import com.rising.pos.feature.transaction.components.TransactionDetailDialog
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.WarningAmber

@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val dateFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Riwayat Transaksi",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Text(
                        text = "${transactions.size} Transaksi Tersimpan",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Belum Ada Transaksi",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Transaksi yang selesai akan tercatat otomatis di sini.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transactions, key = { it.transaction.id }) { item ->
                        TransactionItemCard(
                            trxDetails = item,
                            currencySymbol = settings.currencySymbol,
                            dateStr = dateFormatter.format(Date(item.transaction.createdAt)),
                            onClick = { viewModel.selectTransaction(item) }
                        )
                    }
                }
            }
        }
    }

    // Receipt Detail & Void Dialog
    uiState.selectedTransaction?.let { trx ->
        TransactionDetailDialog(
            transactionWithDetails = trx,
            settings = settings,
            isProcessing = uiState.isProcessing,
            isPrinting = uiState.isPrinting,
            onPrintReceipt = { viewModel.printReceipt(trx) },
            onDismiss = { viewModel.selectTransaction(null) },
            onVoidTransaction = { trxId, reason ->
                viewModel.voidTransaction(trxId, reason)
            }
        )
    }
}

@Composable
private fun TransactionItemCard(
    trxDetails: TransactionWithDetails,
    currencySymbol: String,
    dateStr: String,
    onClick: () -> Unit
) {
    val trx = trxDetails.transaction
    val itemsSummary = trxDetails.items.joinToString(", ") { "${it.item.productName} (${it.item.qty.toInt()})" }
    val isVoided = trx.status == TransactionStatus.CANCELLED
    val isRefunded = trx.status == TransactionStatus.REFUNDED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isVoided) Color(0xFFFAFAFA) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isVoided) 0.5.dp else 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = trx.receiptNumber,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isVoided) Slate500 else Slate900,
                            textDecoration = if (isVoided) TextDecoration.LineThrough else TextDecoration.None
                        )
                    )

                    Surface(
                        color = PrimaryBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = trx.paymentMethod.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (isVoided) {
                        Surface(
                            color = DangerRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "VOID",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DangerRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (isRefunded) {
                        Surface(
                            color = WarningAmber.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "REFUND",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = WarningAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = itemsSummary,
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate700),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.format(trx.grandTotal, currencySymbol),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            isVoided -> DangerRed
                            isRefunded -> WarningAmber
                            else -> SuccessGreen
                        },
                        textDecoration = if (isVoided) TextDecoration.LineThrough else TextDecoration.None
                    )
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Slate500,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

