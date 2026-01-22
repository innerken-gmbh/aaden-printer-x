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
import android.os.RemoteCallbackList
import android.util.Log
import androidx.core.app.NotificationCompat
import com.innerken.aadenprinterx.dataLayer.repository.PrinterRepository
import com.innerken.aadenprinterx.modules.PrinterConnectionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.isNotEmpty

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

    @Volatile
    private var currentState = PrinterConnectionState.DISCONNECTED

    private val statusCallbacks =
        RemoteCallbackList<IPrinterStatusCallback>()


    //AIDL Binder
    private val binder = object : IPrinterService.Stub() {

        override fun ping(): Int {
            return try {
                currentState.ordinal
            } catch (e: Exception) {
                PrinterConnectionState.DISCONNECTED.ordinal
            }
        }

        override fun registerStatusCallback(callback: IPrinterStatusCallback?) {
            callback?.let {
                statusCallbacks.register(it)
                try {
                    it.onStatusChanged(currentState.ordinal)
                } catch (_: Exception) {}
            }
        }

        override fun unregisterStatusCallback(callback: IPrinterStatusCallback?) {
            callback?.let {
                statusCallbacks.unregister(it)
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

        startPolling()
        return START_STICKY
    }

    private fun startPolling() {
        if (isPollingStarted) {
            Log.d("PrinterXService", "Polling already started, skip")
            return
        }

        synchronized(this) {
            if (isPollingStarted) return
            isPollingStarted = true
        }

        serviceScope.launch {
            printerRepository.initializePrinterIfNeeded(this@PrinterXService)

            notifyStatusChanged(PrinterConnectionState.CONNECTING)
            currentState = PrinterConnectionState.CONNECTING
            var lastConnectionState: Boolean? = null
            var currentDelay = interval
            val maxDelay = 15_000L

            val connection = printerRepository.checkConnection()
            if (!connection) {
                printerRepository.updateStatus("No Connection/连接断开", fail, success)
            }

            while (isActive) {
                val connected = try {
                    printerRepository.checkConnection()
                } catch (e: Exception) {
                    false
                }

                if (lastConnectionState != connected) {
                    if (!connected) {
                        printerRepository.updateStatus("No Connection/连接断开", fail, success)
                        currentState = PrinterConnectionState.DISCONNECTED
                        notifyStatusChanged(currentState)
                    } else {
                        printerRepository.updateStatus("Connected/网络已连接", fail, success)
                        currentState = PrinterConnectionState.CONNECTED
                        notifyStatusChanged(currentState)
                    }
                    lastConnectionState = connected
                }

                if(connected) {
                    currentDelay = interval
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
                } else {
                    currentDelay = (currentDelay * 2).coerceAtMost(maxDelay)
                }

                delay(currentDelay)
            }
        }
    }

    private fun notifyStatusChanged(state: PrinterConnectionState) {
        try {
            val n = statusCallbacks.beginBroadcast()
            for (i in 0 until n) {
                try {
                    statusCallbacks.getBroadcastItem(i).onStatusChanged(state.ordinal)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } finally {
            statusCallbacks.finishBroadcast()
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
