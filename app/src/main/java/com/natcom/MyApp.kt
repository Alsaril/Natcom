package com.natcom

import android.app.Application
import android.content.Context

class MyApp : Application() {
    override fun onCreate() {
        instance = this.applicationContext
        super.onCreate()
    }

    companion object {
        var instance: Context? = null
            get
            private set
    }
}
