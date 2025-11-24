package com.events.app.di

import android.content.Context
import com.events.app.data.LocalDataSource
import com.events.app.domain.repositories.auth.AuthRepository
import com.events.app.domain.repositories.events.EventRepository
import com.events.app.domain.repositories.events.EventRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthRepository(@ApplicationContext context: Context) = AuthRepository(context)

    @Provides
    @Singleton
    fun provideLocalDataSource(): LocalDataSource {
        return LocalDataSource()
    }

    @Provides
    @Singleton
    fun provideEventRepository(localDataSource: LocalDataSource): EventRepository {
        return EventRepositoryImpl(localDataSource)
    }
}