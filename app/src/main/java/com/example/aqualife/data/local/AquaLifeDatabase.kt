package com.example.aqualife.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.aqualife.data.local.dao.*
import com.example.aqualife.data.local.entity.*
import com.example.aqualife.data.local.util.Converters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
    entities = [
        FishEntity::class,
        CartEntity::class,
        UserEntity::class,
        OrderEntity::class,
        FavoriteEntity::class,
        NotificationEntity::class
    ],
    version = 6, // Incremented to add imageUrl to NotificationEntity
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AquaLifeDatabase : RoomDatabase() {
    abstract fun fishDao(): FishDao
    abstract fun cartDao(): CartDao
    abstract fun userDao(): UserDao
    abstract fun orderDao(): OrderDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AquaLifeDatabase? = null

        fun getDatabase(context: Context): AquaLifeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AquaLifeDatabase::class.java,
                    "aqualife_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

