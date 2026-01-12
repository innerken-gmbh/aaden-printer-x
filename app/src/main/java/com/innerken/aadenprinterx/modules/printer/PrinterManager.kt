package com.innerken.aadenprinterx.modules.printer

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.sunmi.printerx.PrinterSdk
import com.sunmi.printerx.enums.PrinterInfo

class PrinterManager(private val context: Context) {
    private val appContext = context.applicationContext
    var bonImage: Bitmap? = null
    private var printer: PrinterSdk.Printer? = null
    private var isReady = false

    init {
        try {
            PrinterSdk.getInstance().getPrinter(context, object : PrinterSdk.PrinterListen {
                override fun onDefPrinter(printer: PrinterSdk.Printer) {
                    try {
                        this@PrinterManager.printer = printer
                        isReady = true
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "打印机初始化完成", Toast.LENGTH_SHORT).show()
                        }
                        Log.d ("PrinterManager", "打印机初始化完成")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        isReady = false
                    }
                }

                override fun onPrinters(printers: List<PrinterSdk.Printer>) {
                    Log.d("PrinterManager", "发现打印机数量: ${printers.size}")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            isReady = false
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "未找到打印服务，请检查设备", Toast.LENGTH_SHORT).show()
            }
            Log.e ("PrinterManager", "未找到打印服务，请检查设备")
        }
    }
    fun isReady(): Boolean = isReady

    fun print(content: String?, overrideLineLength: Int? = null) {
        try {
            val currentPrinter = printer
            if (content != null && currentPrinter != null) {
                val executor = PrintExecutor(currentPrinter)
                executor.bonImage = bonImage
                val lineLength = overrideLineLength ?: run {
                    val paperType = currentPrinter.queryApi().getInfo(PrinterInfo.PAPER)
                    if (paperType == "384") 32 else 48 //384像素对应55mm纸
                }
                executor.doPrint(content, lineLength)
            } else if (currentPrinter == null) {
                Log.e("PrintError", "打印失败：Printer对象为空")
            }
        } catch (e: Exception) {
            Log.e("PrintError", "打印失败: ${e.message}", e)
        }
    }


    fun openDrawer() {
        printer?.cashDrawerApi()?.open(null)
    }

    fun destroy() {
        try {
            PrinterSdk.getInstance().destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}