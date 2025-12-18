package com.innerken.aadenprinterx.modules.printer

import android.graphics.Bitmap
import com.sunmi.printerx.PrinterSdk
import org.davidmoten.text.utils.WordWrap
import org.jsoup.Jsoup

class PrintExecutor(private val printer: PrinterSdk.Printer) {

    var bonImage: Bitmap? = null
    private var lineLength = 0

    private fun String.padWithHan(length: Int, left: Boolean): String {
        val realLength =
            this.sumOf {
                if (it.toString().matches(Regex("[\\u4E00-\\u9FA5]+")))
                    2 as Int
                else
                    1
            }
        return if (realLength >= length) {
            this
        } else {
            if (!left) this + " ".repeat(length - realLength)
            else " ".repeat(length - realLength) + this
        }
    }

    private fun renderTable(tableString: String): String {
        val doc = Jsoup.parse(tableString)
        val thElements = doc.select("th")
        val colCount = thElements.size
        val colWidthArray = thElements.map { it.attr("width") }.toMutableList()
        val restCount = colWidthArray.filter { it != "auto" }.sumOf { it.toInt() }
        val autoIndex = colWidthArray.indexOf("auto")
        colWidthArray[autoIndex] = (lineLength - restCount).toString()
        val realColWidthArray = colWidthArray.map { it.toInt() }

        val wrappedCols = mutableListOf<MutableList<String>>()
        var counter = 0
        doc.select("table").first()!!.child(0).children().forEach {
            wrappedCols.add(mutableListOf())
            it.children().forEachIndexed { i, td ->
                wrappedCols[counter].add(
                    WordWrap.from(td.text().trim())
                        .maxWidth(realColWidthArray[i])
                        .breakWords(true)
                        .wrap()
                )
            }
            counter++
        }

        val lines = mutableListOf<String>()
        wrappedCols.forEach { row ->
            val tmp = row.map { it.split('\n') }
            val maxLineCount = tmp.maxOf { it.size }
            for (i in 0 until maxLineCount) {
                var tmpLine = ""
                for (x in 0 until colCount) {
                    tmpLine += tmp[x].getOrElse(i) { "" }.trim().padWithHan(realColWidthArray[x], x > autoIndex)
                }
                lines.add(tmpLine)
            }
        }
        lines[0] = "<BOLD>${lines[0]}</BOLD>"
        return lines.joinToString("\n")
    }

    private fun replaceTable(content: String): String {
        var text = content
        val startToken = "<TABLE>"
        val endToken = "</TABLE>"
        var idx = text.indexOf(startToken)
        while (idx != -1) {
            val endIdx = text.indexOf(endToken)
            val repeater = text.substring(idx, endIdx + endToken.length)
            text = text.replace(repeater, renderTable(repeater))
            idx = text.indexOf(startToken)
        }
        return text
    }

    private fun replaceLineToken(content: String): String {
        var text = content
        val startToken = "<LINE>"
        val endToken = "</LINE>"
        var idx = text.indexOf(startToken)
        while (idx != -1) {
            val endIdx = text.indexOf(endToken)
            val repeater = text.substring(idx + startToken.length, endIdx)
            text = text.replace(text.substring(idx, endIdx + endToken.length), repeater.repeat(lineLength / repeater.length))
            idx = text.indexOf(startToken)
        }
        return text
    }

    private fun replaceLRToken(content: String): String {
        var text = content
        val startToken = "<LR>"
        val endToken = "</LR>"
        val delimiter = "<DR>"
        var idx = text.indexOf(startToken)
        while (idx != -1) {
            val delIdx = text.indexOf(delimiter, idx)
            val endIdx = text.indexOf(endToken, delIdx)
            if (delIdx == -1 || endIdx == -1) {
                idx = text.indexOf(startToken, idx + 1)
                continue
            }
            val left = text.substring(idx + startToken.length, delIdx)
            val right = text.substring(delIdx + delimiter.length, endIdx)
            var padCount = lineLength - calcPrintLength(left) - calcPrintLength(right)
            while (padCount < 0) padCount += lineLength
            text = text.replace(text.substring(idx, endIdx + endToken.length), left + " ".repeat(padCount) + right)
            idx = text.indexOf(startToken)
        }
        return text
    }

    private fun replaceGermanLetter(content: String): String {
        return content.replace("ä","ae").replace("ö","oe").replace("ü","ue")
            .replace("Ä","Ae").replace("Ö","Oe").replace("Ü","Ue").replace("ß","ss")
    }

    //新增缺少的换行
    private fun replaceNoBRToken(content: String): String {
        return content.replace("</C>", "</C><BR>").replace("</CB>", "</CB><BR>")
    }

    private fun render(content: String): String {
        var text = content.replace("\n","<BR>")
        text = replaceGermanLetter(text)
        text = replaceLineToken(text)
        text = replaceLRToken(text)
        text = replaceTable(text)
        text = replaceNoBRToken(text)
        return text
    }

    private fun calcPrintLength(s: String): Int {
        return s.replace("<.+?>".toRegex(), "")
            .replace("[^\\x00-\\xff]".toRegex(), "**").length
    }

    fun doPrint(content: String, lineLength: Int) {
        this.lineLength = lineLength
        val text = render(content)
        //进入翻译器，转化成一个一个的action，然后逐行打印
        PrintTranslator.translate(text, bonImage, printer).forEach { it() }
    }

    companion object {
        private fun stringRepeat(s: String, n: Int): String = s.repeat(n)
    }

}