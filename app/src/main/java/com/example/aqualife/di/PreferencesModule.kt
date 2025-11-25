package com.example.aqualife.di

import android.content.Context
import com.example.aqualife.data.preferences.SearchHistoryPreferences
import com.example.aqualife.data.preferences.SessionPreferences
import com.example.aqualife.data.preferences.ThemePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    // All preference classes (ThemePreferences, SearchHistoryPreferences, SessionPreferences)
    // have @Inject constructors, so Hilt handles them automatically.
    // No manual @Provides needed.
}

