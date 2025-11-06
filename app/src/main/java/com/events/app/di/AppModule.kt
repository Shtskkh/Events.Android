package com.events.app.di

import com.events.app.data.LocalDataSource
import com.events.app.domain.repository.EventRepository
import com.events.app.domain.repository.EventRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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