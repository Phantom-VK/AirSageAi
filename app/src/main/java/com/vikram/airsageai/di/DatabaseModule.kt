package com.vikram.airsageai.di

import com.vikram.airsageai.data.repository.DatabaseRepository
import com.vikram.airsageai.data.repository.FirebaseDatabaseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {
    @Binds
    @Singleton
    abstract fun bindDatabaseRepository(
        firebaseDatabaseRepository: FirebaseDatabaseRepository
    ): DatabaseRepository
}