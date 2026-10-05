package com.rising.pos.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rising.pos.core.model.OrderType
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.core.model.SyncStatus
import com.rising.pos.core.model.TransactionStatus

/**
 * CATATAN TIPE UANG: seluruh nominal uang disimpan sebagai `Long` (rupiah bulat),
 * BUKAN `Double`, untuk menghindari galat pembulatan floating-point pada
 * penjumlahan/pajak/diskon. Kuantitas & stok tetap `Double` karena bisa pecahan
 * (mis. 0.5 kg).
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["receipt_number"], unique = true),
        Index(value = ["created_at"]),
        Index(value = ["status"]),
        Index(value = ["customer_id"]),
        Index(value = ["sync_status"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "receipt_number")
    val receiptNumber: String,
    @ColumnInfo(name = "device_id")
    val deviceId: String = "A01",
    @ColumnInfo(name = "cashier_id")
    val cashierId: String? = null,
    @ColumnInfo(name = "customer_id")
    val customerId: String? = null,
    @ColumnInfo(name = "order_type")
    val orderType: OrderType = OrderType.RETAIL,
    val subtotal: Long,
    val discount: Long = 0L,
    @ColumnInfo(name = "discount_reason")
    val discountReason: String? = null,
    val tax: Long = 0L,
    @ColumnInfo(name = "service_charge")
    val serviceCharge: Long = 0L,
    @ColumnInfo(name = "grand_total")
    val grandTotal: Long,
    @ColumnInfo(name = "payment_amount")
    val paymentAmount: Long,
    @ColumnInfo(name = "change_amount")
    val changeAmount: Long = 0L,
    @ColumnInfo(name = "payment_method")
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    @ColumnInfo(name = "split_payment_method")
    val splitPaymentMethod: PaymentMethod? = null,
    @ColumnInfo(name = "split_amount")
    val splitAmount: Long = 0L,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val note: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING
)

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["transaction_id"]),
        Index(value = ["product_id"])
    ]
)
data class TransactionItemEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "transaction_id")
    val transactionId: String,
    @ColumnInfo(name = "product_id")
    val productId: String,
    @ColumnInfo(name = "product_name")
    val productName: String,
    @ColumnInfo(name = "variant_name")
    val variantName: String? = null,
    val qty: Double,
    @ColumnInfo(name = "unit_price")
    val unitPrice: Long,
    val subtotal: Long,
    val discount: Long = 0L,
    val note: String? = null
)

@Entity(
    tableName = "transaction_item_modifiers",
    foreignKeys = [
        ForeignKey(
            entity = TransactionItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["transaction_item_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["transaction_item_id"])
    ]
)
data class TransactionItemModifierEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "transaction_item_id")
    val transactionItemId: String,
    @ColumnInfo(name = "modifier_id")
    val modifierId: String,
    @ColumnInfo(name = "modifier_name")
    val modifierName: String,
    val price: Long
)
