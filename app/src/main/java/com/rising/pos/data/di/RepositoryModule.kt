package com.rising.pos.data.di

import com.rising.pos.data.repository.ProductRepositoryImpl
import com.rising.pos.data.repository.TransactionRepositoryImpl
import com.rising.pos.domain.repository.ProductRepository
import com.rising.pos.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        impl: com.rising.pos.data.repository.ExpenseRepositoryImpl
    ): com.rising.pos.domain.repository.ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindStockRepository(
        impl: com.rising.pos.data.repository.StockRepositoryImpl
    ): com.rising.pos.domain.repository.StockRepository

    @Binds
    @Singleton
    abstract fun bindCustomerRepository(
        impl: com.rising.pos.data.repository.CustomerRepositoryImpl
    ): com.rising.pos.domain.repository.CustomerRepository
}
