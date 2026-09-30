package com.rising.pos.core.model

enum class BusinessType {
    WARUNG,
    CAFE,
    RETAIL,
    SERVICE,
    EVENT_BOOTH,
    CUSTOM
}

enum class OrderType {
    DINE_IN,
    TAKEAWAY,
    DELIVERY,
    RETAIL
}

enum class PaymentMethod {
    CASH,
    QRIS,
    BANK_TRANSFER,
    DEBIT_CARD,
    CREDIT_CARD,
    E_WALLET,
    OTHER
}

enum class TransactionStatus {
    COMPLETED,
    CANCELLED,
    REFUNDED,
    HELD
}

enum class StockMovementType {
    IN,
    OUT,
    ADJUSTMENT,
    SALE,
    REFUND,
    DAMAGE,
    LOSS
}

enum class SyncStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED,
    CONFLICT
}

enum class AppTheme(val label: String) {
    SYSTEM("Ikuti Sistem"),
    LIGHT("Terang"),
    DARK("Gelap (Hitam)")
}

