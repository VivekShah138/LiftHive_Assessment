package com.example.lifthive

import android.app.Application
import com.example.lifthive.data.local.SeedDataManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LiftHiveApplication : Application() {

    @Inject
    lateinit var seedDataManager: SeedDataManager

    override fun onCreate() {
        super.onCreate()
        // Seed sample data only on first launch (no-op afterwards)
        seedDataManager.seedIfNeeded()
    }
}
