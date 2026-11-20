package mx.cooperativasma.app.di

import mx.cooperativasma.app.data.alert.OrderAlertManagerImpl
import mx.cooperativasma.app.domain.alert.OrderAlertManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindOrderAlertManager(
        orderAlertManagerImpl: OrderAlertManagerImpl
    ): OrderAlertManager
}
