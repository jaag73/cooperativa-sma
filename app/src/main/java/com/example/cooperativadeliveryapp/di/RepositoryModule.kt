package com.example.cooperativadeliveryapp.di

import com.example.cooperativadeliveryapp.data.repository.AuthRepositoryImpl
import com.example.cooperativadeliveryapp.data.repository.BusinessMenuRepositoryImpl
import com.example.cooperativadeliveryapp.data.repository.CartRepositoryImpl
import com.example.cooperativadeliveryapp.data.repository.ClientDiscoveryRepositoryImpl
import com.example.cooperativadeliveryapp.data.repository.CourierLogisticsRepositoryImpl
import com.example.cooperativadeliveryapp.data.repository.OrderLiveRepositoryImpl
import com.example.cooperativadeliveryapp.data.repository.OrderRepositoryImpl
import com.example.cooperativadeliveryapp.data.repository.UserRepositoryImpl
import com.example.cooperativadeliveryapp.domain.repository.AuthRepository
import com.example.cooperativadeliveryapp.domain.repository.BusinessMenuRepository
import com.example.cooperativadeliveryapp.domain.repository.CartRepository
import com.example.cooperativadeliveryapp.domain.repository.ClientDiscoveryRepository
import com.example.cooperativadeliveryapp.domain.repository.CourierLogisticsRepository
import com.example.cooperativadeliveryapp.domain.repository.OrderLiveRepository
import com.example.cooperativadeliveryapp.domain.repository.OrderRepository
import com.example.cooperativadeliveryapp.domain.repository.UserRepository
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
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
    
    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRepositoryImpl: OrderRepositoryImpl
    ): OrderRepository
    
    @Binds
    @Singleton
    abstract fun bindBusinessMenuRepository(
        businessMenuRepositoryImpl: BusinessMenuRepositoryImpl
    ): BusinessMenuRepository

    @Binds
    @Singleton
    abstract fun bindClientDiscoveryRepository(
        clientDiscoveryRepositoryImpl: ClientDiscoveryRepositoryImpl
    ): ClientDiscoveryRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        cartRepositoryImpl: CartRepositoryImpl
    ): CartRepository
    
    @Binds
    @Singleton
    abstract fun bindCourierLogisticsRepository(
        courierLogisticsRepositoryImpl: CourierLogisticsRepositoryImpl
    ): CourierLogisticsRepository
    
    @Binds
    @Singleton
    abstract fun bindOrderLiveRepository(
        orderLiveRepositoryImpl: OrderLiveRepositoryImpl
    ): OrderLiveRepository
}
