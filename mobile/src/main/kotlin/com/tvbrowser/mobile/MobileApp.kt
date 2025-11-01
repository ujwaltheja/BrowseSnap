package com.tvbrowser.mobile

import android.app.Application
import timber.log.Timber

class MobileApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
