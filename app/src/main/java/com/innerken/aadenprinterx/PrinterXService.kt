package com.innerken.aadenprinterx

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.innerken.aadenprinterx.dataLayer.repository.PrinterRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PrinterXService : Service() {

    @Inject
    lateinit var printerRepository: PrinterRepository

    private var fail = 0
    private var success = 0
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val interval = 1000L

    private val NOTIFICATION_CHANNEL_ID = "Printer_Channel"
    private val NOTIFICATION_ID = 101

    private var isPollingStarted = false


    //AIDL Binder
    private val binder = object : IPrinterService.Stub() {
        override fun triggerPrint(): Boolean {
            return try {
                serviceScope.launch {
                    printerRepository.initializePrinterIfNeeded(this@PrinterXService)
                    if (!isPollingStarted) startPolling()
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        override fun ping(): Boolean {
            return try {
                printerRepository.canAcceptPrint()
            } catch (e: Exception) {
                false
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("PrinterXService", "Service onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("PrinterXService", "Service Started")

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            printerRepository.initializePrinterIfNeeded(this@PrinterXService)
            startPolling()
        }

        return START_STICKY
    }

    private fun startPolling() {
        if (isPollingStarted) return
        isPollingStarted = true

        serviceScope.launch {
            val connection = printerRepository.checkConnection()
            if (!connection) {
                printerRepository.updateStatus("No Connection/连接断开", fail, success)
            }

            while (isActive && connection) {
                val list = printerRepository.getShangMiPrintQuest()
                if (list.isNotEmpty()) {
                    for (quest in list) {
                        try {
                            printerRepository.reportStatus(quest.id)
                            printerRepository.tryPrint(quest)
                            success++
                            printerRepository.updateStatus("OK", fail, success)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            fail++
                            printerRepository.updateStatus("ERROR", fail, success)
                        }
                    }
                }
                delay(interval)
            }
        }
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Printer Monitor Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("打印服务正在运行")
            .setContentText("正在监控打印队列，确保及时打印...")
            .setSmallIcon(R.drawable.cottilogo)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("PrinterXService", "Service Stopped and Scope Cancelled")
        serviceScope.cancel()
        stopForeground(true)
    }
}
