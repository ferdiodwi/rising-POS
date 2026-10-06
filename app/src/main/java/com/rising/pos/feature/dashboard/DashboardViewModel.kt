package com.rising.pos.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.TopSellingProduct
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.core.printer.BluetoothPrinterManager
import com.rising.pos.core.printer.ReportPrintData
import com.rising.pos.domain.repository.ExpenseRepository
import com.rising.pos.domain.repository.ProductRepository
import com.rising.pos.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

enum class DashboardPeriod(val label: String) {
    TODAY("Hari ini"),
    LAST_7_DAYS("7 hari"),
    THIS_MONTH("Bulan ini"),
    CUSTOM("Pilih tanggal")
}

data class SalesPoint(
    val label: String,
    val amount: Long,
    val dayName: String = "",
    val isHighlighted: Boolean = false
)

data class TopProductDisplayItem(
    val rank: Int,
    val productId: String,
    val productName: String,
    val categoryName: String,
    val imageUrl: String? = null,
    val totalQtyText: String = ""
)

data class DashboardMetrics(
    val grossSales: Long = 0L,
    val expenses: Long = 0L,
    val netProfit: Long = 0L,
    val transactionCount: Int = 0,
    val averageTicketSize: Long = 0L,
    val totalProductCount: Int = 0,
    val lowStockProducts: List<ProductEntity> = emptyList(),
    val topSellingProducts: List<TopSellingProduct> = emptyList(),
    val topProductsDisplay: List<TopProductDisplayItem> = emptyList(),
    val dailySales: List<SalesPoint> = emptyList(),
    val paymentSales: Map<PaymentMethod, Long> = emptyMap(),
    val isSampleData: Boolean = false,
    val dateRangeText: String = "",
    val monthRangeText: String = "",
    val trendPercentage: String = "",
    val highestDayText: String = "",
    val dailyAverageSales: Long = 0L
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val productRepository: ProductRepository,
    private val expenseRepository: ExpenseRepository,
    private val appPreferences: AppPreferences,
    private val printerManager: BluetoothPrinterManager
) : ViewModel() {

    val settings: StateFlow<BusinessSettings> = appPreferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BusinessSettings()
    )

    private val _isPrinting = MutableStateFlow(false)
    val isPrinting: StateFlow<Boolean> = _isPrinting.asStateFlow()

    private val _printMessage = MutableStateFlow<String?>(null)
    val printMessage: StateFlow<String?> = _printMessage.asStateFlow()

    fun printReport() {
        val currentSettings = settings.value
        if (currentSettings.printerMacAddress.isBlank()) {
            _printMessage.value = "Printer thermal belum dipilih di Pengaturan."
            return
        }

        val m = metrics.value
        val reportData = ReportPrintData(
            title = "REKAP PENJUALAN",
            dateRangeText = m.dateRangeText.ifBlank { selectedPeriod.value.label },
            grossSales = m.grossSales,
            transactionCount = m.transactionCount,
            averageTicketSize = m.averageTicketSize,
            expenses = m.expenses,
            netProfit = m.netProfit,
            paymentSales = m.paymentSales,
            topSellingProducts = m.topSellingProducts
        )

        viewModelScope.launch {
            _isPrinting.value = true
            _printMessage.value = null
            val result = printerManager.printReport(
                macAddress = currentSettings.printerMacAddress,
                reportData = reportData,
                settings = currentSettings
            )
            _isPrinting.value = false
            _printMessage.value = if (result.isSuccess) {
                "Laporan berhasil dicetak ke printer."
            } else {
                result.exceptionOrNull()?.localizedMessage ?: "Gagal mencetak laporan"
            }
        }
    }

    fun clearPrintMessage() {
        _printMessage.value = null
    }

    private val _selectedPeriod = MutableStateFlow(DashboardPeriod.LAST_7_DAYS)
    val selectedPeriod: StateFlow<DashboardPeriod> = _selectedPeriod.asStateFlow()

    private val _customDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val customDateRange: StateFlow<Pair<Long, Long>?> = _customDateRange.asStateFlow()

    fun setPeriod(period: DashboardPeriod) {
        _selectedPeriod.value = period
    }

    fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        _customDateRange.value = Pair(startMillis, endMillis)
        _selectedPeriod.value = DashboardPeriod.CUSTOM
    }

    private fun getPeriodDateRange(period: DashboardPeriod, customRange: Pair<Long, Long>?): Pair<Long, Long> {
        val endCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endTime = endCal.timeInMillis

        val startTime = when (period) {
            DashboardPeriod.TODAY -> {
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            DashboardPeriod.LAST_7_DAYS -> {
                Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -6)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            DashboardPeriod.THIS_MONTH -> {
                Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            DashboardPeriod.CUSTOM -> {
                customRange?.first ?: Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
        }
        val finalEnd = if (period == DashboardPeriod.CUSTOM && customRange != null) customRange.second else endTime
        return Pair(startTime, finalEnd)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val metrics: StateFlow<DashboardMetrics> = combine(
        _selectedPeriod,
        _customDateRange
    ) { period, customRange -> Pair(period, customRange) }
        .flatMapLatest { (period, customRange) ->
            val (startTime, endTime) = getPeriodDateRange(period, customRange)
            combine(
                combine(
                    transactionRepository.getGrossSalesBetween(startTime, endTime),
                    expenseRepository.getTotalExpenseBetween(startTime, endTime),
                    transactionRepository.getTransactionCountBetween(startTime, endTime)
                ) { sales, exp, count -> Triple(sales, exp, count) },
                transactionRepository.getTopSellingProductsBetween(startTime, endTime, 5),
                productRepository.getProductCount(),
                productRepository.getLowStockProducts(),
                transactionRepository.getCompletedTransactionsBetween(startTime, endTime)
            ) { (sales, expenses, count), topSelling, productCount, lowStock, completed ->
                val gross = sales ?: 0L
                val exp = expenses ?: 0L
                val net = gross - exp
                val avgTicket = if (count > 0) gross / count else 0L

                val zone = ZoneId.systemDefault()
                val today = LocalDate.now(zone)
                val sevenDays = (6 downTo 0).map { today.minusDays(it.toLong()) }
                val idLocale = Locale.forLanguageTag("id-ID")

                val completedByDay = completed.groupBy {
                    Instant.ofEpochMilli(it.transaction.createdAt).atZone(zone).toLocalDate()
                }
                val dailyPoints = if (period == DashboardPeriod.LAST_7_DAYS) {
                    sevenDays.mapIndexed { idx, date ->
                        val entries = completedByDay[date] ?: emptyList()
                        val dayAbbr = date.format(DateTimeFormatter.ofPattern("EEE", idLocale)).replaceFirstChar { it.uppercase() }
                        SalesPoint(
                            label = date.dayOfMonth.toString(),
                            amount = entries.sumOf { it.transaction.grandTotal },
                            dayName = dayAbbr,
                            isHighlighted = idx == sevenDays.lastIndex
                        )
                    }
                } else {
                    completedByDay.toSortedMap().map { (date, entries) ->
                        val dayAbbr = date.format(DateTimeFormatter.ofPattern("EEE", idLocale)).replaceFirstChar { it.uppercase() }
                        SalesPoint(
                            label = date.dayOfMonth.toString(),
                            amount = entries.sumOf { it.transaction.grandTotal },
                            dayName = dayAbbr
                        )
                    }
                }

                val breakdown = mutableMapOf<PaymentMethod, Long>()
                for (item in completed) {
                    val trx = item.transaction
                    if (trx.splitPaymentMethod != null && trx.splitAmount > 0) {
                        val amount1 = (trx.grandTotal - trx.splitAmount).coerceAtLeast(0L)
                        breakdown[trx.paymentMethod] = (breakdown[trx.paymentMethod] ?: 0L) + amount1
                        breakdown[trx.splitPaymentMethod] = (breakdown[trx.splitPaymentMethod] ?: 0L) + trx.splitAmount
                    } else {
                        breakdown[trx.paymentMethod] = (breakdown[trx.paymentMethod] ?: 0L) + trx.grandTotal
                    }
                }
                val paymentBreakdown = breakdown.toMap()

                val firstDate = if (period == DashboardPeriod.LAST_7_DAYS) sevenDays.first() else Instant.ofEpochMilli(startTime).atZone(zone).toLocalDate()
                val lastDate = if (period == DashboardPeriod.LAST_7_DAYS) sevenDays.last() else Instant.ofEpochMilli(endTime).atZone(zone).toLocalDate()

                val dateRangeText = if (firstDate == lastDate) {
                    firstDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", idLocale))
                } else {
                    "${firstDate.format(DateTimeFormatter.ofPattern("d MMM", idLocale))} – ${lastDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", idLocale))}"
                }

                val m1 = firstDate.format(DateTimeFormatter.ofPattern("MMMM", idLocale)).replaceFirstChar { it.uppercase() }
                val m2 = lastDate.format(DateTimeFormatter.ofPattern("MMMM", idLocale)).replaceFirstChar { it.uppercase() }
                val monthRangeText = if (m1 == m2) m1 else "$m1 – $m2"

                val maxPoint = dailyPoints.maxByOrNull { it.amount }
                val highestDay = if (maxPoint != null && maxPoint.amount > 0L) {
                    if (maxPoint.dayName.isNotBlank()) maxPoint.dayName else "Tgl ${maxPoint.label}"
                } else ""
                val daysInPeriod = (java.time.temporal.ChronoUnit.DAYS.between(firstDate, lastDate) + 1).coerceAtLeast(1L)
                val dailyAvg = gross / daysInPeriod

                val topDisplay = topSelling.mapIndexed { idx, p ->
                    TopProductDisplayItem(
                        rank = idx + 1,
                        productId = p.productId,
                        productName = p.productName,
                        categoryName = "Produk",
                        totalQtyText = "${java.math.BigDecimal.valueOf(p.totalQty).stripTrailingZeros().toPlainString()} pcs"
                    )
                }

                DashboardMetrics(
                    grossSales = gross,
                    expenses = exp,
                    netProfit = net,
                    transactionCount = count,
                    averageTicketSize = avgTicket,
                    totalProductCount = productCount,
                    lowStockProducts = lowStock,
                    topSellingProducts = topSelling,
                    topProductsDisplay = topDisplay,
                    dailySales = dailyPoints,
                    paymentSales = paymentBreakdown,
                    isSampleData = false,
                    dateRangeText = dateRangeText,
                    monthRangeText = monthRangeText,
                    trendPercentage = "",
                    highestDayText = if (highestDay.isBlank()) "Belum ada penjualan" else "Tertinggi: $highestDay",
                    dailyAverageSales = dailyAvg
                )
            }
        }.combine(productRepository.getAllProducts()) { summary, products ->
            summary.copy(topProductsDisplay = summary.topProductsDisplay.map { ranked ->
                val item = products.firstOrNull { it.product.id == ranked.productId }
                ranked.copy(imageUrl = item?.product?.imageUrl, categoryName = item?.category?.name ?: ranked.categoryName)
            })
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardMetrics()
        )
}
