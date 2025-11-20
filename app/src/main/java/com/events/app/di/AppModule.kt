package com.events.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.events.app.data.AuthRepository
import com.events.app.data.LocalDataSource
import com.events.app.domain.repositories.events.EventRepository
import com.events.app.domain.repositories.events.EventRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Это расширение для создания DataStore
val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Новый провайдер для DataStore
    @Provides
    @Singleton
    fun provideAuthDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.authDataStore
    }

    // Новый провайдер для AuthRepository
    @Provides
    @Singleton
    fun provideAuthRepository(dataStore: DataStore<Preferences>): AuthRepository {
        return AuthRepository(dataStore)
    }

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