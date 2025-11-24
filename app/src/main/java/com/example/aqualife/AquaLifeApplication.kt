package com.example.aqualife

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AquaLifeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

