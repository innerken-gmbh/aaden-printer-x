package com.innerken.aadenprinterx.dataLayer.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.scale
import androidx.lifecycle.viewModelScope
import com.innerken.aadenprinterx.dataLayer.api.PrinterService
import com.innerken.aadenprinterx.dataLayer.model.PrintQuestEntity
import com.innerken.aadenprinterx.dataLayer.model.RestaurantInfoEntity
import com.innerken.aadenprinterx.modules.GlobalSettingManager
import com.innerken.aadenprinterx.modules.RefreshIntervalMs
import com.innerken.aadenprinterx.modules.network.SafeRequest
import com.innerken.aadenprinterx.modules.printer.PrinterManager
import com.innerken.aadenprinterx.modules.printingThis
import com.innerken.aadenprinterx.modules.targetPattern
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import java.util.Date
import java.util.LinkedList
import java.util.regex.Matcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.times

@Singleton
class PrinterRepository @Inject constructor(
    private val printerService: PrinterService,
    private val okHttpClient: OkHttpClient,
    private val globalSettingManager: GlobalSettingManager,
) {
    private val _status = MutableStateFlow("Ready")
    val status: StateFlow<String> = _status

    private val _countText = MutableStateFlow("0/0")
    val countText: StateFlow<String> = _countText

    // 暴露一个方法供 Service 调用来更新状态
    fun updateStatus(newStatus: String, fail: Int, success: Int) {
        _status.value = newStatus
        _countText.value = "$fail/${fail + success}"
    }
    var printerManager: PrinterManager? = null

    //这里是之前集成在我们自己的快消软件
    val printQuests: Flow<List<PrintQuestEntity>> = flow {
        while (true) { //这里之前集成在我们软件上是做了一个开关，可以由商户自行决定是否打印
            val newPrintQuest = getShangMiPrintQuest()
            emit(newPrintQuest)
            delay(RefreshIntervalMs)
        }
    }

    //获取之前历史打印内容
    val oldPrintRecord: Flow<List<PrintQuestEntity>> = flow {
        while (true) {
            val newPrintQuest = getOldPrintRecord()
            emit(newPrintQuest)
            delay(RefreshIntervalMs * 15)
        }
    }

    private val printQueue: MutableMap<Int, String> = mutableMapOf()

    //执行打印操作
    fun tryPrint(p: PrintQuestEntity) {
        if (!printQueue.containsKey(p.id) && p.content != null) {
            printQueue[p.id] = p.content
            if (p.printerGroupId != 8 && p.content.isNotBlank()) {
                doPrint(p.content) //执行
            }
        } else {
            Log.e("DISCARD", "ID:${p.id}")
        }
    }


    suspend fun getShangMiPrintQuest(): List<PrintQuestEntity> {
        return try {

            val result = SafeRequest.handle { printerService.getShangMiPrintQuest() } ?: emptyList()

            result.map {
                if (it.title == null) {
                    it.title = "No Title"
                }

                val targetMatcher: Matcher = targetPattern.matcher(it.title ?: "")
                if (targetMatcher.find()) {
                    it.targetId = targetMatcher.group(1)
                }
                it
            }.filter {
                printingThis(
                    it.targetId,
                    globalSettingManager.getFilterSet(),
                    globalSettingManager.getFilterMode()
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    suspend fun reportStatus(id: Int): Any {
        return SafeRequest.handle { printerService.reportPrintStatus(id) } ?: -1
    }

    private suspend fun getOldPrintRecord(): List<PrintQuestEntity> {
        return (SafeRequest.handle { printerService.getPrintQuestEntities() }
            ?: listOf()).map {
            if (it.title == null) {
                it.title = "No Title"
            }
            it
        }
    }

    //钱箱
    fun openDrawer() {
        try {
            printerManager?.openDrawer()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun doPrint(content: String) {
        printerManager?.print(
            content, null
        )

    }

    fun havePrinterManager(): Boolean {
        return printerManager != null
    }
    suspend fun getPrintBonImage(): Bitmap? {
        return try {
            val req = Request.Builder()
                .url((globalSettingManager.getResourceUrl() + "printImg/bonLogo.png"))
                .build()

            val res = okHttpClient.newCall(req).await()
            val stream = res.body.byteStream()
            val map = BitmapFactory.decodeStream(stream)
            val resizedBitmap = map.scale(576, map.height * 576 / map.width, false)
            return resizedBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getBonImage() {
        printerManager?.bonImage = getPrintBonImage()

    }

    suspend fun getRestaurantInfo(): RestaurantInfoEntity? {
        return SafeRequest.handle { printerService.getRestaurantInfo() }?.get(0)
    }

    //获取餐馆信息来判定ip是否正确
    suspend fun checkConnection(): Boolean {
        return try {
            val info = getRestaurantInfo()
            info != null
        } catch (e: Exception) {
            false
        }
    }

}
