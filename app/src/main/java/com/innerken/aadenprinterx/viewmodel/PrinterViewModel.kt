package com.innerken.aadenprinterx.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.innerken.aadenprinterx.dataLayer.model.PrintQuestEntity
import com.innerken.aadenprinterx.dataLayer.repository.PrinterRepository
import com.innerken.aadenprinterx.modules.GlobalSettingManager
import com.innerken.aadenprinterx.modules.printer.PrinterManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class PrinterViewModel @Inject constructor(
    private val printerRepository: PrinterRepository,
    val globalSettingManager: GlobalSettingManager,
) : ViewModel() {

    val status = printerRepository.status
    val countText = printerRepository.countText

//    init {
//        viewModelScope.launch {
//            printerRepository.printQuests.collect { printerQuests ->
//                if (printerQuests.isNotEmpty() && printerRepository.havePrinterManager()) {
//                    printerQuests.forEach { quest ->
//                        try {
//                            printerRepository.reportStatus(quest.id)
//                            sendLocalPrint(quest)
//                            success++
//                            status.value = "OK"
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                            fail++
//                            status.value = "ERROR"
//                        } finally {
//                            countText.value = "$fail/${fail + success}"
//                        }
//                    }
//                }
//            }
//        }
//
//    }

    fun initPrinterManager(printerManager: PrinterManager) {
        printerRepository.printerManager = printerManager
    }

    suspend fun setBonImage() {
        withContext(Dispatchers.IO) {
            printerRepository.getBonImage()
        }
    }
}
