package com.tvbrowser.mobile

import android.app.Application
import com.tvbrowser.mobile.di.AppContainer
import timber.log.Timber

class MobileApp : Application() {

    // AppContainer instance used by the rest of classes to obtain dependencies
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize dependency injection container
        appContainer = AppContainer(this)

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("BrowseSnap Mobile App initialized")
    }
}
