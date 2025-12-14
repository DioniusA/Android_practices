package com.example.recipeplanner

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class for Recipe Planner.
 * Initializes Hilt dependency injection and Timber logging.
 */
@HiltAndroidApp
class RecipePlannerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
