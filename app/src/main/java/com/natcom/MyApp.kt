package com.natcom

import android.app.Application
import android.content.Context

class MyApp : Application() {
    override fun onCreate() {
        instance = this.applicationContext
        super.onCreate()
    }

    companion object {
        lateinit var instance: Context
            get
            private set
    }
}
