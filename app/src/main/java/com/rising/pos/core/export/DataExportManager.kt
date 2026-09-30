package com.rising.pos.core.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.rising.pos.core.database.PosDatabase
import com.rising.pos.domain.repository.ExpenseRepository
import com.rising.pos.domain.repository.ProductRepository
import com.rising.pos.domain.repository.TransactionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExportManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: PosDatabase,
    private val transactionRepository: TransactionRepository,
    private val productRepository: ProductRepository,
    private val expenseRepository: ExpenseRepository
) {
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())

    private fun getExportDir(): File {
        val dir = File(context.cacheDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getBackupDir(): File {
        val dir = File(context.cacheDir, "backups")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun createShareIntent(file: File, mimeType: String, title: String): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    suspend fun exportTransactionsCsv(currencySymbol: String = "Rp"): Result<Intent> = withContext(Dispatchers.IO) {
        try {
            val transactions = transactionRepository.getRecentTransactions(limit = 1000).first()
            val csvContent = CsvExporter.generateTransactionsCsv(transactions, currencySymbol)
            val timestamp = fileDateFormat.format(Date())
            val file = File(getExportDir(), "transaksi_$timestamp.csv")
            file.writeText(csvContent, Charsets.UTF_8)

            val shareIntent = createShareIntent(file, "text/csv", "Ekspor Transaksi ($timestamp)")
            Result.success(shareIntent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportProductsCsv(currencySymbol: String = "Rp"): Result<Intent> = withContext(Dispatchers.IO) {
        try {
            val products = productRepository.getAllProducts().first()
            val csvContent = CsvExporter.generateProductsCsv(products, currencySymbol)
            val timestamp = fileDateFormat.format(Date())
            val file = File(getExportDir(), "katalog_produk_$timestamp.csv")
            file.writeText(csvContent, Charsets.UTF_8)

            val shareIntent = createShareIntent(file, "text/csv", "Ekspor Katalog Produk ($timestamp)")
            Result.success(shareIntent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportExpensesCsv(currencySymbol: String = "Rp"): Result<Intent> = withContext(Dispatchers.IO) {
        try {
            val expenses = expenseRepository.getAllExpenses().first()
            val csvContent = CsvExporter.generateExpensesCsv(expenses, currencySymbol)
            val timestamp = fileDateFormat.format(Date())
            val file = File(getExportDir(), "pengeluaran_$timestamp.csv")
            file.writeText(csvContent, Charsets.UTF_8)

            val shareIntent = createShareIntent(file, "text/csv", "Ekspor Pengeluaran ($timestamp)")
            Result.success(shareIntent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDatabaseBackup(): Result<Intent> = withContext(Dispatchers.IO) {
        try {
            // 1. Force checkpoint WAL to ensure all transactions are persisted to the main db file
            try {
                database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
            } catch (_: Exception) {}

            val originalDbFile = context.getDatabasePath("rising_pos.db")
            if (!originalDbFile.exists()) {
                return@withContext Result.failure(IllegalStateException("File database belum dibuat."))
            }

            val timestamp = fileDateFormat.format(Date())
            val backupFile = File(getBackupDir(), "rising_pos_backup_$timestamp.db")
            originalDbFile.copyTo(backupFile, overwrite = true)

            val shareIntent = createShareIntent(
                backupFile,
                "application/octet-stream",
                "Cadangan Database POS ($timestamp)"
            )
            Result.success(shareIntent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
