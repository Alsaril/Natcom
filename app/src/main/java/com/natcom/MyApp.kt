package com.natcom

import android.app.Application
import android.content.Context
import android.os.StrictMode


class MyApp : Application() {
    override fun onCreate() {
        instance = this.applicationContext

        super.onCreate()
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
    }

    companion object {
        lateinit var instance: Context
            get
            private set
    }
}
