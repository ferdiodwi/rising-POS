package com.rising.pos.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.TopSellingProduct
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
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
import java.util.Calendar
import javax.inject.Inject
import com.rising.pos.core.model.PaymentMethod
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class DashboardPeriod(val label: String) {
    TODAY("Hari Ini"),
    LAST_7_DAYS("7 Hari"),
    THIS_MONTH("Bulan Ini"),
    ALL_TIME("Semua")
}

data class SalesPoint(val label: String, val amount: Long)

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
    val todayGrossSales: Long = grossSales,
    val todayExpenses: Long = expenses,
    val todayTransactionCount: Int = transactionCount
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

    private val _selectedPeriod = MutableStateFlow(DashboardPeriod.TODAY)
    val selectedPeriod: StateFlow<DashboardPeriod> = _selectedPeriod.asStateFlow()

    fun setPeriod(period: DashboardPeriod) {
        _selectedPeriod.value = period
    }

    private fun getPeriodDateRange(period: DashboardPeriod): Pair<Long, Long> {
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
            DashboardPeriod.ALL_TIME -> 0L
        }

        return Pair(startTime, endTime)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val metrics: StateFlow<DashboardMetrics> = _selectedPeriod.flatMapLatest { period ->
        val (startTime, endTime) = getPeriodDateRange(period)
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
            DashboardMetrics(
                grossSales = gross,
                expenses = exp,
                netProfit = net,
                transactionCount = count,
                averageTicketSize = avgTicket,
                totalProductCount = productCount,
                lowStockProducts = lowStock,
                topSellingProducts = topSelling,
                dailySales = completed.groupBy {
                    val date = Instant.ofEpochMilli(it.transaction.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
                    if (period == DashboardPeriod.ALL_TIME) date.withDayOfMonth(1) else date
                }.toSortedMap().map { (date, entries) ->
                    SalesPoint(
                        date.format(DateTimeFormatter.ofPattern(if (period == DashboardPeriod.ALL_TIME) "MMM yy" else "dd MMM", java.util.Locale.forLanguageTag("id-ID"))),
                        entries.sumOf { it.transaction.grandTotal }
                    )
                },
                paymentSales = completed.groupBy { it.transaction.paymentMethod }
                    .mapValues { (_, entries) -> entries.sumOf { it.transaction.grandTotal } },
                todayGrossSales = gross,
                todayExpenses = exp,
                todayTransactionCount = count
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardMetrics()
    )
}
