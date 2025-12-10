package com.innerken.aadenprinterx

import android.app.Application
import android.content.Intent
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AadenApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val intent = Intent(this, PrinterXService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}