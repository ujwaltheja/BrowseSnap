package com.tvbrowser.tv

import android.app.Application
import timber.log.Timber

class TVApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
