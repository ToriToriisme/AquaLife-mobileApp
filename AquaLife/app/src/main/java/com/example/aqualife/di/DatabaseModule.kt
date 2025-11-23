package com.example.aqualife.di

import android.content.Context
import androidx.room.Room
import com.example.aqualife.data.local.AquaLifeDatabase
import com.example.aqualife.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AquaLifeDatabase {
        return Room.databaseBuilder(
            context,
            AquaLifeDatabase::class.java,
            "aqualife_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideFishDao(database: AquaLifeDatabase): FishDao = database.fishDao()

    @Provides
    fun provideCartDao(database: AquaLifeDatabase): CartDao = database.cartDao()

    @Provides
    fun provideUserDao(database: AquaLifeDatabase): UserDao = database.userDao()

    @Provides
    fun provideOrderDao(database: AquaLifeDatabase): OrderDao = database.orderDao()

    @Provides
    fun provideFavoriteDao(database: AquaLifeDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    fun provideNotificationDao(database: AquaLifeDatabase): NotificationDao = database.notificationDao()
}

