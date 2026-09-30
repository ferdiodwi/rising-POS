package com.rising.pos.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.domain.repository.ProductRepository
import com.rising.pos.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class DashboardMetrics(
    val todayGrossSales: Double = 0.0,
    val todayTransactionCount: Int = 0,
    val averageTicketSize: Double = 0.0,
    val totalProductCount: Int = 0,
    val lowStockProducts: List<ProductEntity> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val productRepository: ProductRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val settings: StateFlow<BusinessSettings> = appPreferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BusinessSettings()
    )

    private val startOfDay: Long
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

    private val endOfDay: Long
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            return cal.timeInMillis
        }

    val metrics: StateFlow<DashboardMetrics> = combine(
        transactionRepository.getGrossSalesBetween(startOfDay, endOfDay),
        transactionRepository.getTransactionCountBetween(startOfDay, endOfDay),
        productRepository.getProductCount(),
        productRepository.getLowStockProducts()
    ) { sales, count, productCount, lowStock ->
        val grossSales = sales ?: 0.0
        val avgTicket = if (count > 0) grossSales / count else 0.0
        DashboardMetrics(
            todayGrossSales = grossSales,
            todayTransactionCount = count,
            averageTicketSize = avgTicket,
            totalProductCount = productCount,
            lowStockProducts = lowStock
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardMetrics()
    )
}
