package com.rising.pos.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.TopSellingProduct
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.model.PaymentMethod
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
    CUSTOM("Pilih tanggal")
}

data class SalesPoint(
    val label: String,
    val amount: Long,
    val isHighlighted: Boolean = false
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
    val dailySales: List<SalesPoint> = emptyList(),
    val paymentSales: Map<PaymentMethod, Long> = emptyMap(),
    val isSampleData: Boolean = false,
    val dateRangeText: String = "",
    val monthRangeText: String = ""
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val productRepository: ProductRepository,
    private val expenseRepository: ExpenseRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val settings: StateFlow<BusinessSettings> = appPreferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BusinessSettings()
    )

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

                val isSample = count == 0 && gross == 0L

                if (isSample) {
                    val sampleDailySales = listOf(
                        SalesPoint("26", 155_000L),
                        SalesPoint("27", 210_000L),
                        SalesPoint("28", 174_000L),
                        SalesPoint("29", 132_000L),
                        SalesPoint("30", 198_000L),
                        SalesPoint("1", 190_000L),
                        SalesPoint("2", 186_000L, isHighlighted = true)
                    )
                    val samplePayment = mapOf(
                        PaymentMethod.CASH to 830_000L,
                        PaymentMethod.QRIS to 290_000L,
                        PaymentMethod.BANK_TRANSFER to 125_000L
                    )
                    val sampleProducts = listOf(
                        TopSellingProduct("s1", "Indomie Goreng", 52.0, 52 * 3500L),
                        TopSellingProduct("s2", "Teh Botol 350 ml", 31.0, 31 * 5000L),
                        TopSellingProduct("s3", "Minyak Goreng 1 L", 18.0, 18 * 14000L)
                    )

                    val (dispGross, dispCount, dispAvg) = if (period == DashboardPeriod.TODAY) {
                        Triple(186_000L, 7, 26_571L)
                    } else {
                        Triple(1_245_000L, 45, 27_667L)
                    }

                    val dateRangeText = if (period == DashboardPeriod.TODAY) {
                        today.format(DateTimeFormatter.ofPattern("d MMMM yyyy", idLocale))
                    } else {
                        "26 Sep – 2 Okt 2026"
                    }

                    DashboardMetrics(
                        grossSales = dispGross,
                        expenses = 0L,
                        netProfit = dispGross,
                        transactionCount = dispCount,
                        averageTicketSize = dispAvg,
                        totalProductCount = productCount,
                        lowStockProducts = lowStock,
                        topSellingProducts = sampleProducts,
                        dailySales = sampleDailySales,
                        paymentSales = samplePayment,
                        isSampleData = true,
                        dateRangeText = dateRangeText,
                        monthRangeText = "September – Oktober"
                    )
                } else {
                    val completedByDay = completed.groupBy {
                        Instant.ofEpochMilli(it.transaction.createdAt).atZone(zone).toLocalDate()
                    }
                    val dailyPoints = if (period == DashboardPeriod.LAST_7_DAYS) {
                        sevenDays.mapIndexed { idx, date ->
                            val entries = completedByDay[date] ?: emptyList()
                            SalesPoint(
                                label = date.dayOfMonth.toString(),
                                amount = entries.sumOf { it.transaction.grandTotal },
                                isHighlighted = idx == sevenDays.lastIndex
                            )
                        }
                    } else {
                        completedByDay.toSortedMap().map { (date, entries) ->
                            SalesPoint(
                                label = date.dayOfMonth.toString(),
                                amount = entries.sumOf { it.transaction.grandTotal }
                            )
                        }
                    }

                    val paymentBreakdown = completed.groupBy { it.transaction.paymentMethod }
                        .mapValues { (_, entries) -> entries.sumOf { it.transaction.grandTotal } }

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

                    DashboardMetrics(
                        grossSales = gross,
                        expenses = exp,
                        netProfit = net,
                        transactionCount = count,
                        averageTicketSize = avgTicket,
                        totalProductCount = productCount,
                        lowStockProducts = lowStock,
                        topSellingProducts = topSelling,
                        dailySales = dailyPoints,
                        paymentSales = paymentBreakdown,
                        isSampleData = false,
                        dateRangeText = dateRangeText,
                        monthRangeText = monthRangeText
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardMetrics()
        )
}
