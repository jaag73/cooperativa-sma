package mx.cooperativasma.app.di

import mx.cooperativasma.app.data.repository.AuthRepositoryImpl
import mx.cooperativasma.app.data.repository.BusinessMenuRepositoryImpl
import mx.cooperativasma.app.data.repository.CartRepositoryImpl
import mx.cooperativasma.app.data.repository.ClientDiscoveryRepositoryImpl
import mx.cooperativasma.app.data.repository.CourierLogisticsRepositoryImpl
import mx.cooperativasma.app.data.repository.OrderLiveRepositoryImpl
import mx.cooperativasma.app.data.repository.OrderRepositoryImpl
import mx.cooperativasma.app.data.repository.UserRepositoryImpl
import mx.cooperativasma.app.domain.repository.AuthRepository
import mx.cooperativasma.app.domain.repository.BusinessMenuRepository
import mx.cooperativasma.app.domain.repository.CartRepository
import mx.cooperativasma.app.domain.repository.ClientDiscoveryRepository
import mx.cooperativasma.app.domain.repository.CourierLogisticsRepository
import mx.cooperativasma.app.domain.repository.OrderLiveRepository
import mx.cooperativasma.app.domain.repository.OrderRepository
import mx.cooperativasma.app.domain.repository.UserRepository
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
