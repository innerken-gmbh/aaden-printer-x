package com.innerken.aadenprinterx.dataLayer.model

import android.util.Log
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

data class PrintQuestEntity(
    val id: Int,
    val printStatus: Int,
    val printerGroupId: Int,
    val content: String?,
    val addTimeStamp: String,
    val orderId: String?,
    var title: String?,
    var targetId: String? = null,
) {
    private fun getRenderReadyContent(): String {
        return "<PRINTER>" + content?.replace("<BR>", "\n")
            ?.replace("<DR>", "-*-")?.replace("&", " and ") +
                "</PRINTER>"
    }

    //单据预览功能，调用可以直接生成可预览账单
    fun getDomList(): Element? {
        val builder = DocumentBuilderFactory.newInstance()
        val b = builder.newDocumentBuilder()
        val doc =
            try {
                b.parse(getRenderReadyContent().byteInputStream())
            } catch (e: Exception) {
                Log.e("PARSE_FAILED", e.message ?: "")
                b.parse("<OK></OK>".byteInputStream())
            }

        return doc.documentElement
    }
}