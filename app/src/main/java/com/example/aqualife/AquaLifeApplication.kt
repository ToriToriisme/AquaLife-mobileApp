package com.example.aqualife

import android.app.Application
import android.util.Log
import com.example.aqualife.data.migration.FirebaseImageMigration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AquaLifeApplication : Application() {
    
    @Inject
    lateinit var firebaseMigration: FirebaseImageMigration
    
    override fun onCreate() {
        super.onCreate()
        
        // Auto-migrate Firebase images on app start (only once)
        // Uncomment the line below to enable auto-migration
        // firebaseMigration.migrateFirebaseImages()
        
        Log.d("AquaLifeApp", "Application started. Migration available but disabled by default.")
        Log.d("AquaLifeApp", "To enable migration, uncomment line in AquaLifeApplication.onCreate()")
    }
}

