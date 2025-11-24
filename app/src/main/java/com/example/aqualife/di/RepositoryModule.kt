package com.example.aqualife.di

import com.example.aqualife.data.repository.FishRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    // Repositories are already injected via constructor
    // This module can be used for additional repository bindings if needed
}

